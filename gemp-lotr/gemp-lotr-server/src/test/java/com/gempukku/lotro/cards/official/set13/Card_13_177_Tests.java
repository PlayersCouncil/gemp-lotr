package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_177_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("control", "13_177");
					put("control2", "13_177");
					put("commander", "13_158"); // Assault Commander: [uruk-hai] Uruk-hai, strength 9; a Shadow ability used only on purpose
					put("saruman", "17_116");   // Saruman, Master of the White Hand: [uruk-hai] Wizard, strength 8; companions resistance -1, a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, Damage +1 and nothing else

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: abilities used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: ally, strength 4; his maneuver ability lets him skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeaponsofControlStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Weapons of Control
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, add an [uruk-hai] token here for each wounded companion.<br><b>Skirmish:</b> Discard this from play or remove 2 tokens from here to make a companion skirmishing an [uruk-hai] minion strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("control");

		assertEquals("Weapons of Control", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedItTakesATokenForEachWoundedCompanionAndForNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(thrarin);

		var control = scn.GetShadowCard("control");
		var control2 = scn.GetShadowCard("control2");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(control);
		scn.MoveCardsToSupportArea(control2);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		// Two wounded companions, one of them with two wounds; an unwounded companion, a wounded ally and a wounded minion.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(commander, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(commander));
		assertInZone(Zone.SUPPORT, control2);
		assertEquals(0, scn.GetCultureTokensOn(control2));
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(control));
		scn.ShadowPlayCard(control);

		// Frodo and Aragorn, each once; the tokens go here, not on the other copy.
		assertInZone(Zone.SUPPORT, control);
		assertEquals(2, scn.GetCultureTokensOn(control));
		assertEquals(0, scn.GetCultureTokensOn(control2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneWoundedCompanionItTakesOneTokenAndAnotherCopyPlayedLaterLeavesItAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var control = scn.GetShadowCard("control");
		var control2 = scn.GetShadowCard("control2");
		scn.MoveCardsToHand(control, control2);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();

		scn.ShadowPlayCard(control);
		assertEquals(1, scn.GetCultureTokensOn(control));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second copy's play is no play of this one: it takes its own token, and this one keeps its one.
		scn.ShadowPlayCard(control2);
		assertInZone(Zone.SUPPORT, control2);
		assertEquals(1, scn.GetCultureTokensOn(control2));
		assertEquals(1, scn.GetCultureTokensOn(control));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ItsSkirmishAbilityMakesTheCompanionSkirmishingAnUrukHaiMinionStrengthMinus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var control = scn.GetShadowCard("control");
		var control2 = scn.GetShadowCard("control2");
		var commander = scn.GetShadowCard("commander");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(control, control2);
		// This copy holds 1 token, the other 2.
		scn.AddTokensToCard(control, 1);
		scn.AddTokensToCard(control2, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, control, control2);
		assertEquals(1, scn.GetCultureTokensOn(control));
		assertEquals(2, scn.GetCultureTokensOn(control2));
		scn.MoveMinionsToTable(commander, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{gimli, saruman});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowActionAvailable(control));
		scn.ShadowUseCardAction(control);

		// With 1 token the discard is the only way to pay, and is taken with nothing asked; the other copy keeps its tokens.
		assertInDiscard(control);
		assertInZone(Zone.SUPPORT, control2);
		assertEquals(2, scn.GetCultureTokensOn(control2));
		// Aragorn, against the [uruk-hai] Uruk-hai; not Gimli, whose skirmish with the [uruk-hai] Wizard has not begun.
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// In Gimli's skirmish, a phase of its own, Aragorn's penalty is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowUseCardAction(control2);

		// Both costs can be paid: the player is asked which, and the 2 tokens removed keep the card in play.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Remove 2 tokens"));
		scn.ShadowChooseOption("Remove 2 tokens");
		assertInZone(Zone.SUPPORT, control2);
		assertEquals(0, scn.GetCultureTokensOn(control2));
		assertEquals(5, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItsSkirmishAbilityIsPaidForNothingWhereNoCompanionSkirmishesAnUrukHaiMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var control = scn.GetShadowCard("control");
		var control2 = scn.GetShadowCard("control2");
		var commander = scn.GetShadowCard("commander");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(control, control2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, control, control2);
		scn.MoveMinionsToTable(commander, guard);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		// A companion against a [sauron] Uruk-hai; an ally against an [uruk-hai] Uruk-hai.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{thrarin, commander});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(control));
		scn.ShadowUseCardAction(control);
		assertInDiscard(control);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(control2));
		scn.ShadowUseCardAction(control2);
		assertInDiscard(control2);
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItsSkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var control = scn.GetShadowCard("control");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToSupportArea(control);
		scn.AddTokensToCard(control, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(control));
		scn.MoveMinionsToTable(commander);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(control));
	}
}
