package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slunk", "10_115");
					put("slunk2", "10_115");
					put("slunk3", "10_115");
					put("slunk4", "10_115");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, enduring;
					                            // his skirmish ability is used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home site 2; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("ted", "18_75");        // Ted Sandyman: the opponent's Hobbit, a minion; maneuver and regroup abilities only
					// Uruk Pursuer: [isengard] Uruk-hai, printed fierce; his other texts are about unbound Hobbits
					// discarded or killed, and none is
					put("pursuer", "4_190");

					// For the discard pile: [shire] cards of five types, and a [gondor] card.
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("appetite", "1_294");   // Hobbit Appetite: a [shire] event
					put("gaffer", "1_291");     // The Gaffer: a [shire] ally
					put("talent", "1_316");     // A Talent for Not Being Seen: a [shire] condition
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] companion
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a [shire] ally
					put("swordarm", "1_116");   // Swordarm of the White Tower: a [gondor] event
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
					put("slunk", "10_115");
					put("slunk2", "10_115");
					put("slunk3", "10_115");
					put("slunk4", "10_115");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, enduring;
					                            // his skirmish ability is used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home site 2; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("ted", "18_75");        // Ted Sandyman: the opponent's Hobbit, a minion; maneuver and regroup abilities only
					// Uruk Pursuer: [isengard] Uruk-hai, printed fierce; his other texts are about unbound Hobbits
					// discarded or killed, and none is
					put("pursuer", "4_190");

					// For the discard pile: [shire] cards of five types, and a [gondor] card.
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("appetite", "1_294");   // Hobbit Appetite: a [shire] event
					put("gaffer", "1_291");     // The Gaffer: a [shire] ally
					put("talent", "1_316");     // A Talent for Not Being Seen: a [shire] condition
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] companion
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a [shire] ally
					put("swordarm", "1_116");   // Swordarm of the White Tower: a [gondor] event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void SlunkOutofSightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Slunk Out of Sight
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>. If there are 6 or more [shire] cards in your discard pile, cancel a skirmish involving your Hobbit and a minion who is not fierce. Each minion in that skirmish is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slunk");

		assertEquals("Slunk Out of Sight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithSixShireCardsInTheDiscardPileItCancelsYourHobbitsSkirmishAndMakesItsMinionsFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slunk = scn.GetFreepsCard("slunk");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var appetite = scn.GetFreepsCard("appetite");
		var gaffer = scn.GetFreepsCard("gaffer");
		var talent = scn.GetFreepsCard("talent");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var swordarm = scn.GetFreepsCard("swordarm");
		var shadowTalent = scn.GetShadowCard("talent");
		scn.MoveCompanionsToTable(pippin, aragorn);
		// Five [shire] cards and a [gondor] one in his own discard pile, and a [shire] card in his opponent's.
		scn.MoveCardsToDiscard(sword, appetite, gaffer, talent, sam, swordarm, shadowTalent);
		scn.MoveCardsToHand(slunk);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInDiscard(sword, appetite, gaffer, talent, sam, swordarm, shadowTalent);
		scn.MoveMinionsToTable(savage, brigand, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{aragorn, troop});
		// The Shadow player puts the minion left over on Pippin too.
		scn.ShadowAssignToMinions(pippin, brigand);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(slunk));

		scn.MoveCardsToDiscard(bilbo);
		assertInDiscard(bilbo);
		assertTrue(scn.FreepsPlayAvailable(slunk));

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		scn.FreepsPlayCard(slunk);

		// Pippin's skirmish is cancelled; both its minions are fierce, and the Orc waiting for Aragorn is not.
		assertInDiscard(slunk);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		scn.PassSkirmishActions();

		// Still fierce in the fierce assignment phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Gone at the regroup phase.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishWithNoHobbitOfYoursOrOnlyAFierceMinionIsNotCancelledButAHobbitAllysIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slunk = scn.GetFreepsCard("slunk");
		var slunk2 = scn.GetFreepsCard("slunk2");
		var slunk3 = scn.GetFreepsCard("slunk3");
		var slunk4 = scn.GetFreepsCard("slunk4");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var appetite = scn.GetFreepsCard("appetite");
		var gaffer = scn.GetFreepsCard("gaffer");
		var talent = scn.GetFreepsCard("talent");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToDiscard(sword, appetite, gaffer, talent, sam, bilbo);
		scn.MoveCardsToHand(slunk, slunk2, slunk3, slunk4);

		var savage = scn.GetShadowCard("savage");
		var ted = scn.GetShadowCard("ted");
		var pursuer = scn.GetShadowCard("pursuer");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertInDiscard(sword, appetite, gaffer, talent, sam, bilbo);
		scn.MoveMinionsToTable(savage, ted, pursuer, brigand);
		scn.SkipToAssignments();
		// Site 2 is Bounder's home: he may be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, savage}, new PhysicalCardImpl[]{aragorn, ted},
				new PhysicalCardImpl[]{pippin, pursuer}, new PhysicalCardImpl[]{bounder, brigand});

		// A [shire] companion who is no Hobbit, against a minion who is not fierce: played, and nothing more.
		scn.FreepsResolveSkirmish(smeagol);
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.FreepsPlayCard(slunk);
		assertInDiscard(slunk);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A Hobbit who is not his (the opponent's minion), against a companion who is no Hobbit.
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		scn.FreepsPlayCard(slunk2);
		assertInDiscard(slunk2);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// His Hobbit against a fierce minion.
		scn.FreepsResolveSkirmish(pippin);
		scn.FreepsPlayCard(slunk3);
		assertInDiscard(slunk3);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// His Hobbit ally against a minion who is not fierce: cancelled.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, brigand));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		scn.FreepsPlayCard(slunk4);
		assertInDiscard(slunk4);
		assertFalse(scn.IsCharSkirmishing(bounder));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheRingBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var slunk = scn.GetFreepsCard("slunk");
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var appetite = scn.GetFreepsCard("appetite");
		var gaffer = scn.GetFreepsCard("gaffer");
		var talent = scn.GetFreepsCard("talent");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToDiscard(sword, appetite, gaffer, talent, sam, bilbo);
		scn.MoveCardsToHand(slunk);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInDiscard(sword, appetite, gaffer, talent, sam, bilbo);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.FreepsPlayCard(slunk);

		assertInDiscard(slunk);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelledThoughItsMinionIsMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slunk = scn.GetFreepsCard("slunk");
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var appetite = scn.GetFreepsCard("appetite");
		var gaffer = scn.GetFreepsCard("gaffer");
		var talent = scn.GetFreepsCard("talent");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToDiscard(sword, appetite, gaffer, talent, sam, bilbo);
		scn.MoveCardsToHand(slunk);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInDiscard(sword, appetite, gaffer, talent, sam, bilbo);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.FreepsPlayAvailable(slunk));
		scn.FreepsPlayCard(slunk);

		// The skirmish goes on, and its minion is made fierce all the same: the second sentence names the
		// skirmish the text describes, and is read as not hanging on the cancel being carried out.
		assertInDiscard(slunk);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
