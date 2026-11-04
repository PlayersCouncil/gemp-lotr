package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_162_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crouched", "11_162");
					put("crouched2", "11_162");
					put("crouched3", "11_162");

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, resistance 6, enduring;
					                            //   his skirmish ability is used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, resistance 6; his skirmish
					                            //   ability is used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion, resistance 5; a fellowship
					                            //   ability and a response to Frodo's death
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, resistance 6
					put("bounder", "1_286");    // Bounder: a Hobbit ally (resistance 0), home site 2; a skirmish ability used
					                            //   only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("savage2", "1_151");
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block format, in which a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crouched", "11_162");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void CrouchedDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Crouched Down
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.<br>Cancel a skirmish involving a Hobbit and a minion whose strength is lower than that Hobbit's resistance.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crouched");

		assertEquals("Crouched Down", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CancelsTheSkirmishOfAHobbitOfEitherCultureAgainstAMinionWeakerThanHisResistanceEvenBesideAStrongerOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crouched = scn.GetFreepsCard("crouched");
		var crouched2 = scn.GetFreepsCard("crouched2");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, merry, aragorn);
		scn.MoveCardsToHand(crouched, crouched2);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var explorer = scn.GetShadowCard("explorer");
		var troop = scn.GetShadowCard("troop");

		assertInHand(crouched, crouched2);
		scn.StartGame();
		scn.MoveMinionsToTable(savage, brigand, explorer, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{merry, explorer},
				new PhysicalCardImpl[]{aragorn, troop});
		// The Shadow player puts the minion left over on Pippin too: strength 8, not lower than his 6.
		scn.ShadowAssignToMinions(pippin, brigand);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, brigand));
		assertEquals(6, scn.GetResistance(pippin));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsPlayAvailable(crouched));
		scn.FreepsPlayCard(crouched);

		// Strength 5 is lower than resistance 6: Pippin's skirmish is cancelled, the Brigand in it all the same.
		assertInDiscard(crouched);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// A [shire] Hobbit, against a minion of another culture and race.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, explorer));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(5, scn.GetStrength(explorer));
		scn.FreepsPlayCard(crouched2);

		assertInDiscard(crouched2);
		assertFalse(scn.IsCharSkirmishing(merry));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ASkirmishIsNotCancelledWhereTheMinionIsAsStrongAsThatHobbitsResistanceOrTheCharacterIsNoHobbitOrAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crouched = scn.GetFreepsCard("crouched");
		var crouched2 = scn.GetFreepsCard("crouched2");
		var crouched3 = scn.GetFreepsCard("crouched3");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(crouched, crouched2, crouched3);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var explorer = scn.GetShadowCard("explorer");

		assertInHand(crouched, crouched2, crouched3);
		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, explorer);
		scn.SkipToAssignments();
		// Site 2 is Bounder's home: he may be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{smeagol, savage2},
				new PhysicalCardImpl[]{bounder, explorer});

		// Sam's resistance 5 against strength 5: not lower, though Frodo's 10 is on the table.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsPlayCard(crouched);
		assertInDiscard(crouched);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A [shire] companion of resistance 6 who is no Hobbit, against strength 5.
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(6, scn.GetResistance(smeagol));
		scn.FreepsPlayCard(crouched2);
		assertInDiscard(crouched2);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A Hobbit ally: an ally's resistance is 0, and no minion's strength is lower.
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(0, scn.GetResistance(bounder));
		scn.FreepsPlayCard(crouched3);
		assertInDiscard(crouched3);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, explorer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ItCannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crouched = scn.GetFreepsCard("crouched");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(crouched);

		var savage = scn.GetShadowCard("savage");

		assertInHand(crouched);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(crouched));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(pippin, savage);
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(crouched));
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheRingBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var crouched = scn.GetFreepsCard("crouched");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(crouched);

		var savage = scn.GetShadowCard("savage");

		assertInHand(crouched);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPlayCard(crouched);

		assertInDiscard(crouched);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crouched = scn.GetFreepsCard("crouched");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(crouched);

		var savage = scn.GetShadowCard("savage");

		assertInHand(crouched);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertTrue(scn.FreepsPlayAvailable(crouched));
		scn.FreepsPlayCard(crouched);

		assertInDiscard(crouched);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
