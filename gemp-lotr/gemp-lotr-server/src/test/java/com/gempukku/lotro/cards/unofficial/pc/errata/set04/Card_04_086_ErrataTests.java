package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_086_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thonnas", "54_86");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion: a companion who is no Elf
					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area, a card of the culture that is no Elf; nothing without Legolas
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, cost 2, archer
					put("guardian", "4_76");    // Lorien Guardian: Elf companion; a regroup ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, archer
					put("arwensbow", "18_4");   // Arwen's Bow: [elven] ranged weapon, strength +1; its ability works only for Arwen
					put("elvenbow", "4_62");    // Elven Bow: [elven] ranged weapon, cost 1; bearer is an archer
					put("sword", "4_64");       // Elven Sword: [elven] hand weapon, strength +1; a skirmish ability used only on purpose
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThonnasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Thónnas, Naith Captain
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * 	To play, spot an Elf.
		 * 	While Thónnas bears a ranged weapon, each minion skirmishing him is strength -2 and Thónnas does not add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thonnas");

		assertEquals("Thónnas", card.getBlueprint().getTitle());
		assertEquals("Naith Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndThenPlaysForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var finalcount = scn.GetFreepsCard("finalcount");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(thonnas, orophin);

		scn.StartGame();

		// A companion who is no Elf and an [elven] condition in play, and no Elf.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(thonnas));

		scn.FreepsPlayCard(orophin);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(thonnas));

		scn.FreepsPlayCard(thonnas);
		assertInZone(Zone.FREE_CHARACTERS, thonnas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedBySpottingAnElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(guardian);
		scn.MoveCardsToHand(thonnas);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(thonnas));

		scn.FreepsPlayCard(thonnas);
		assertInZone(Zone.FREE_CHARACTERS, thonnas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileBearingARangedWeaponEachMinionSkirmishingThonnasIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var guardian = scn.GetFreepsCard("guardian");
		var arwensbow = scn.GetFreepsCard("arwensbow");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveCompanionsToTable(thonnas, guardian);
		scn.AttachCardsTo(thonnas, arwensbow);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToAssignments();
		// Put on after the archery phase: it makes the Guardian an archer.
		scn.AttachCardsTo(guardian, elvenbow);

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thonnas, troop1},
				new PhysicalCardImpl[]{guardian, troop3});
		// The Free Peoples player may put only one minion on each; the Shadow player adds the second.
		scn.ShadowAssignToMinions(thonnas, troop2);
		scn.FreepsResolveSkirmish(guardian);

		// The other companion bears a ranged weapon too, and two minions are assigned to Thónnas
		// whose skirmish has not begun: none of them is skirmishing him.
		assertEquals(13, scn.GetStrength(troop3));
		assertEquals(13, scn.GetStrength(troop1));
		assertEquals(13, scn.GetStrength(troop2));

		scn.PassSkirmishActions();
		// The Guardian (5) is overwhelmed by the troop (13), which stays on the table.
		scn.FreepsResolveSkirmish(thonnas);

		assertEquals(11, scn.GetStrength(troop1));
		assertEquals(11, scn.GetStrength(troop2));
		assertEquals(13, scn.GetStrength(troop3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThonnasDoesNotAddToTheFellowshipArcheryTotalOnceHeBearsARangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var legolas = scn.GetFreepsCard("legolas");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var arwensbow = scn.GetFreepsCard("arwensbow");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(thonnas, legolas);
		scn.MoveCardsToHand(elvenbow);
		// Legolas bears a ranged weapon too, and is an archer already: he must go on adding to the total.
		scn.AttachCardsTo(legolas, arwensbow);
		// An archer before he bears the bow, so that the bow's own keyword is not what changes.
		scn.ApplyAdHocModifier(new AddKeywordModifier(thonnas, thonnas, null, Keyword.ARCHER));

		scn.StartGame();

		// Thónnas and Legolas
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Legolas already bears a ranged weapon, so Thónnas is the only bearer there is and is chosen for the player.
		scn.FreepsPlayCard(elvenbow);
		assertAttachedTo(elvenbow, thonnas);
		assertTrue(scn.HasKeyword(thonnas, Keyword.ARCHER));

		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ARCHERY);
		// Legolas alone, though he bears a ranged weapon as well
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void BearingOnlyAHandWeaponThonnasWeakensNoMinionAndAddsToTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thonnas = scn.GetFreepsCard("thonnas");
		var sword = scn.GetFreepsCard("sword");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(thonnas);
		scn.AttachCardsTo(thonnas, sword);
		scn.ApplyAdHocModifier(new AddKeywordModifier(thonnas, thonnas, null, Keyword.ARCHER));

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.PassArcheryActions();
		// The fellowship's one archery wound, on the only minion.
		assertEquals(1, scn.GetWoundsOn(troop1));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(thonnas, troop1);
		scn.FreepsResolveSkirmish(thonnas);

		assertEquals(13, scn.GetStrength(troop1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
