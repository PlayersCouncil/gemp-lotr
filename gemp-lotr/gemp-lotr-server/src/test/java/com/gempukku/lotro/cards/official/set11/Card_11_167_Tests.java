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

public class Card_11_167_Tests
{

	// The Fellowship path, with Streets of Bree laid after site 1 where a dwelling is wanted.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("incognito", "11_167");
					put("incognito2", "11_167");
					put("incognito3", "11_167");
					put("incognito4", "11_167");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, vitality 4 (cheated in); a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4; abilities used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3, vitality 4; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, strength 2, vitality 2; a skirmish ability used only on purpose
					put("follow", "1_109");     // One Whom Men Would Follow: [gondor] maneuver event, exert Aragorn so that an ally takes part in skirmishes

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					// Uruk Savage: strength 5 and Damage +1, no other text.
					put("savage1", "1_151");
					put("savage2", "1_151");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Site 2 is Ettenmoors 1_331, a plains (no dwelling), whose skirmish ability is used only on purpose.
					// Streets of Bree: a Shadows-block dwelling with no other keyword, laid on the path as site 2 by
					// MoveCardToAdventurePath where a test wants one; a bar on transferring followers.
					put("bree", "18_140");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block format, where a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("incognito", "11_167");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion; a skirmish ability used only on purpose
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Valley of the Silverlode: a Shadows-block dwelling and river, laid on the path as site 2 by
					// MoveCardToAdventurePath; "When your fellowship moves to this site, you may heal each Hobbit
					// companion" (nobody is wounded).
					put("valley", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void IncognitoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Incognito
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.
		 * If the fellowship is at a dwelling site, exert 2 Hobbits to cancel a skirmish involving a Hobbit.
		 * At any other site, exert 2 Hobbits to make a Hobbit strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("incognito");

		assertEquals("Incognito", card.getBlueprint().getTitle());
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
	public void AtADwellingItExertsTwoHobbitsOfAnySideToCancelASkirmishInvolvingAHobbitOfAnyCultureOrSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var incognito2 = scn.GetFreepsCard("incognito2");
		var incognito3 = scn.GetFreepsCard("incognito3");
		var incognito4 = scn.GetFreepsCard("incognito4");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var ted = scn.GetShadowCard("ted");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry, pippin, sam, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		// Sam, vitality 4 with 3 wounds, cannot exert.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveCardsToHand(incognito, incognito2, incognito3, incognito4, follow);
		scn.MoveMinionsToTable(troop1, troop2, troop3, ted);
		scn.SkipToPhase(Phase.MANEUVER);
		assertSame(bree, scn.GetCurrentSite());
		// A Hobbit ally fights at his home site only, and Streets of Bree is none: he is made to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop1}, new PhysicalCardImpl[]{pippin, troop2},
				new PhysicalCardImpl[]{aragorn, ted}, new PhysicalCardImpl[]{bounder, troop3});

		// A [shire] Hobbit companion's skirmish. The cost: exactly 2 Hobbits who can exert, of any culture, type or
		// side (a bare race word in a cost names none); not Sam, exhausted, nor Smeagol or Aragorn, who are no Hobbits.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.FreepsPlayAvailable(incognito));
		scn.FreepsPlayCard(incognito);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, smeagol, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(frodo, ted);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertInDiscard(incognito);

		// A [gondor] Hobbit companion's.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop2));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsPlayCard(incognito2);
		scn.FreepsChooseCards(frodo, merry);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertInDiscard(incognito2);

		// The Shadow player's Hobbit, a minion.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.FreepsPlayCard(incognito3);
		scn.FreepsChooseCards(frodo, pippin);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(ted));
		assertInDiscard(incognito3);

		// A Hobbit ally's.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop3));
		scn.FreepsPlayCard(incognito4);
		scn.FreepsChooseCards(merry, pippin);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertFalse(scn.IsCharSkirmishing(bounder));
		assertFalse(scn.IsCharSkirmishing(troop3));
		assertInDiscard(incognito4);

		// Nobody fought: the only wounds are the exertions, and Bounder, Ted and Aragorn took no more.
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtADwellingASkirmishInvolvingNoHobbitIsNotCancelledAndNoHobbitIsMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop1 = scn.GetShadowCard("troop1");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry, smeagol);

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveCardsToHand(incognito);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertSame(bree, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(smeagol, troop1);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));
		// Frodo 3, +1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(incognito));

		// Smeagol is [shire] and no Hobbit: the event is played and its cost paid, and nothing is cancelled. At a
		// dwelling the second sentence is not reached either: no Hobbit is asked for or made stronger.
		// Frodo and Merry are the only Hobbits, so they exert unasked.
		scn.FreepsPlayCard(incognito);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInDiscard(incognito);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveCardsToHand(incognito);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertSame(bree, scn.GetCurrentSite());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(incognito));

		// In the skirmish that follows, it is.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(incognito));
	}

	@Test
	public void WithOnlyOneHobbitAbleToExertItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop1 = scn.GetShadowCard("troop1");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry, smeagol);
		// Merry, vitality 4 with 3 wounds, cannot exert; Frodo can, and Smeagol, who could, is no Hobbit.
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveCardsToHand(incognito);
		assertInHand(incognito);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertSame(bree, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(smeagol, troop1);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsPlayAvailable(incognito));
		assertInHand(incognito);
	}

	@Test
	public void AtAnyOtherSiteItExertsTwoHobbitsToMakeAHobbitOfAnyCultureOrSideStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var savage1 = scn.GetShadowCard("savage1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveCardsToHand(incognito);
		scn.MoveMinionsToTable(savage1, ted);
		scn.SkipToAssignments();
		// Ettenmoors, a plains: no dwelling.
		assertSame(scn.GetFreepsSite(2).getBlueprint(), scn.GetCurrentSite().getBlueprint());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage1}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage1));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsPlayAvailable(incognito));

		scn.FreepsPlayCard(incognito);
		assertEquals(2, scn.FreepsGetChoiceMin());
		// Pippin is enduring (strength +2 for each wound), so he is not the one exerted.
		scn.FreepsChooseCards(frodo, bounder);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));

		// Hobbits in this skirmish, in another, and in none, companions, an ally and a minion of both sides; not
		// Smeagol, Aragorn or the Savage.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, frodo, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, savage1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertInDiscard(incognito);
		// 3 + 3
		assertEquals(6, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		// Nothing is cancelled away from a dwelling.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthBonusLastsOnlyForTheSkirmishPhaseItWasMadeIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(merry, pippin, sam);

		scn.StartGame();
		scn.MoveCardsToHand(incognito);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage1}, new PhysicalCardImpl[]{pippin, savage2});
		scn.FreepsResolveSkirmish(merry);

		// Pippin, who waits for his own skirmish, is the Hobbit made stronger during Merry's.
		assertEquals(3, scn.GetStrength(pippin));
		scn.FreepsPlayCard(incognito);
		scn.FreepsChooseCards(frodo, sam);
		scn.FreepsChooseCard(pippin);
		assertEquals(6, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(pippin));

		// In the next skirmish phase, his own, the bonus is gone.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, savage2));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheRingBearersSkirmishAtADwellingThatIsARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveCardToAdventurePath(valley);
		scn.MoveCardsToHand(incognito);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertSame(valley, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		// Frodo and Merry are the only Hobbits, so they exert unasked.
		scn.FreepsPlayCard(incognito);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertInDiscard(incognito);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathItIsPlayedAndTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var incognito = scn.GetFreepsCard("incognito");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveCardToAdventurePath(bree);
		scn.MoveCardsToHand(incognito);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertSame(bree, scn.GetCurrentSite());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(incognito));
		// Frodo and Merry are the only Hobbits, so they exert unasked.
		scn.FreepsPlayCard(incognito);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInDiscard(incognito);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
