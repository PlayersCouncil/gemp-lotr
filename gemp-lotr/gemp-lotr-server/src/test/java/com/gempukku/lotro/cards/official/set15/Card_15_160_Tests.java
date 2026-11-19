package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_160_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("following", "15_160");
					put("following2", "15_160");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, vitality 3; a skirmish ability never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard, vitality 4; his text is about his strength
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, vitality 2; a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FollowingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Following Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 1</b>.
		 * 	Each time this minion wins a skirmish, you may exert a companion not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("following");

		assertEquals("Following Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenItWinsACompanionNotAssignedToASkirmishMayBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf);
		scn.MoveAlliesToTable(thrarin);

		var following = scn.GetShadowCard("following");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(following, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, following}, new PhysicalCardImpl[]{gimli, troop});

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// 8 + 1 (hunter) against Aragorn's 8: it wins, and Aragorn takes a wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(following));
		scn.ShadowAcceptOptionalTrigger();

		// Frodo and Gandalf, assigned to no skirmish.  Not Aragorn, in this one; not Gimli, whose skirmish is still
		// to come; not the ally, and not a minion, though each could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, gimli, thrarin, following, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WhenItLosesNothingIsOfferedAndTheOtherCopysWinOffersOnlyItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var following = scn.GetShadowCard("following");
		var following2 = scn.GetShadowCard("following2");

		scn.StartGame();
		scn.MoveMinionsToTable(following, following2);
		// Aragorn is made strong enough to beat it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, following}, new PhysicalCardImpl[]{gimli, following2});

		assertEquals(0, scn.GetWoundsOn(following));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// 9 against 13: it loses, takes a wound and lives.  Nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(following));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(following));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(following2));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The other copy, 9 against Gimli's 6, wins: only its own trigger is offered.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(following2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(following));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(following));
		// It is fierce: the game goes on to the fierce assignment phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionOutsideASkirmishAbleToExertNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var following = scn.GetShadowCard("following");

		scn.StartGame();
		// Frodo, the one companion left out of the skirmish, is exhausted (vitality 4).
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(following);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, following);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// It wins.  Aragorn could exert, but he is in the skirmish; so could the ally and the minion.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(following));
		// It is fierce: the game goes on to the fierce assignment phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningTheExertionExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var following = scn.GetShadowCard("following");

		scn.StartGame();
		scn.MoveMinionsToTable(following);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, following);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(following));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// It is fierce: the game goes on to the fierce assignment phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ItWinsTheFierceSkirmishTooAndIsOfferedEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var following = scn.GetShadowCard("following");

		scn.StartGame();
		scn.MoveMinionsToTable(following);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, following);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(following));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// It is fierce, and beats Aragorn a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, following);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(following));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
