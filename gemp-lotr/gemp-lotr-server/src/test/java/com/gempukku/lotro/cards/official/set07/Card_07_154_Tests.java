package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_154_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo, who is kept out of the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("newstrength", "7_154");

					// Easterling Infantry: [raider] Easterling Man, strength 8; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Southron Explorer: [raider] Southron Man, strength 5; a regroup ability only
					put("explorer", "4_250");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul), strength 12, fierce;
					// his strength bonus for Easterlings needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, vitality 1; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability that is never used here

					// Cards that only fill the Free Peoples hand, for initiative (four or more in hand)
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewStrengthCameNowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: New Strength Came Now
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [raider] Man strength +3 (or +6 if you have initiative).
		*/

		var scn = GetScenario();

		var newstrength = scn.GetFreepsCard("newstrength");

		assertEquals("New Strength Came Now", newstrength.getBlueprint().getTitle());
		assertNull(newstrength.getBlueprint().getSubtitle());
		assertFalse(newstrength.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, newstrength.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, newstrength.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, newstrength.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(newstrength, Timeword.SKIRMISH));
		assertEquals(2, newstrength.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithoutInitiativeARaiderManOfALaterSkirmishIsStrengthPlusThreeUntilThisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var newstrength = scn.GetShadowCard("newstrength");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(newstrength);
		// Four cards in the Free Peoples hand: the Shadow has no initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"),
				scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(newstrength);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer, khamul, brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInHand(newstrength);
		assertFalse(scn.ShadowPlayAvailable(newstrength));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, infantry}, new PhysicalCardImpl[]{gimli, explorer});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(newstrength));

		scn.ShadowPlayCard(newstrength);
		// A [raider] Man in this skirmish and one assigned to a later one, an Easterling and a Southron; not the
		// [raider] Nazgul, the [dunland] Man or the [gondor] Man companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, aragorn, gimli, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(explorer);

		assertEquals(8, scn.GetStrength(explorer));
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertInDiscard(newstrength);
		// Still in force at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(explorer));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);

		// The bonus was for that skirmish phase: in his own skirmish the Explorer is back to 5.
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithInitiativeARaiderManIsStrengthPlusSixUntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var newstrength = scn.GetShadowCard("newstrength");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(newstrength);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, infantry}, new PhysicalCardImpl[]{gimli, explorer});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.ShadowPlayAvailable(newstrength));

		scn.ShadowPlayCard(newstrength);
		scn.ShadowChooseCard(infantry);

		assertEquals(14, scn.GetStrength(infantry));
		assertEquals(5, scn.GetStrength(explorer));
		assertInDiscard(newstrength);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 14: Aragorn loses and takes a wound; in the next skirmish the Infantry is 8 again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(infantry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoRaiderManInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var newstrength = scn.GetShadowCard("newstrength");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(newstrength);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		// Its target is no requirement of its play (CR 4.2 "effect").
		assertTrue(scn.ShadowPlayAvailable(newstrength));

		scn.ShadowPlayCard(newstrength);

		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(newstrength);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
