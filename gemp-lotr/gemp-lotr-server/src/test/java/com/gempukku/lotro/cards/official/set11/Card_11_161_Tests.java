package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_161_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("concerning", "11_161");

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, enduring;
					                            //   his skirmish ability is used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, strength 3; his skirmish
					                            //   ability is used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, strength 2; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability
					put("ted", "18_75");        // Ted Sandyman: the opponent's Hobbit, a minion, strength 3; maneuver and
					                            //   regroup abilities only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Site 1 is Bag End, which has no terrain.
					put("site2", "4_330");      // Derndingle: a forest; "While you can spot an unbound Hobbit at Derndingle,
					                            //   the move limit is +1 for this turn"
					put("site3", "1_342");      // Rivendell Waterfall: a forest and a sanctuary; a move-limit text for a ranger
					remove("site8");
					remove("site9");
					// A Shadows-block site, laid on the path by MoveCardToAdventurePath as site 4.
					put("bree", "18_140");      // Streets of Bree: a dwelling; a bar on transferring followers
					// A second Shadows-block site, laid ahead of the fellowship as site 5 where a test reads it.
					put("valley", "11_261");    // Valley of the Silverlode: a dwelling and a river; "When your fellowship
					                            //   moves to this site, you may heal each Hobbit companion"
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ConcerningHobbitsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Concerning Hobbits
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: For each dwelling and forest site on the adventure path, make a Hobbit strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("concerning");

		assertEquals("Concerning Hobbits", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AHobbitOfAnyCultureTypeOrSideIsOfferedAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concerning = scn.GetFreepsCard("concerning");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(pippin, merry, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(concerning);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("bree"));
		scn.MoveMinionsToTable(savage, ted);
		assertInHand(concerning);
		scn.FreepsPass();
		assertSame(scn.GetShadowSite("bree"), scn.GetCurrentSite());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(pippin);
		scn.FreepsPlayCard(concerning);

		assertEquals(1, scn.FreepsGetChoiceMin());
		// The Ring-bearer, a [gondor] Hobbit in this skirmish, a [shire] Hobbit in none, a Hobbit ally and the
		// opponent's Hobbit, a minion in another skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, merry, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose"));
	}

	@Test
	public void FourSitesOfTheTerrainsMakeFourChoicesOfAHobbitWhichMayBeSpread() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concerning = scn.GetFreepsCard("concerning");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(pippin, merry, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(concerning);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("bree"));
		scn.MoveMinionsToTable(savage, ted);
		assertInHand(concerning);
		scn.FreepsPass();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("valley"));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(pippin);

		// On the path: Bag End (none), Derndingle (forest), Rivendell Waterfall (forest and sanctuary), Balin's Tomb and
		// the Bridge of Khazad-dum (underground), Galadriel's Glade (a sanctuary and nothing else), Streets of Bree (dwelling),
		// and Valley of the Silverlode (dwelling and river), ahead of the fellowship: four sites counted.
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsPlayCard(concerning);

		scn.FreepsChooseCard(pippin);
		// One site's +1, and the next choice is asked.
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, merry, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, savage));
		scn.FreepsChooseCard(pippin);
		assertEquals(5, scn.GetStrength(pippin));
		scn.FreepsChooseCard(ted);
		assertEquals(4, scn.GetStrength(ted));
		scn.FreepsChooseCard(merry);

		assertInDiscard(concerning);
		assertEquals(5, scn.GetStrength(pippin)); // 3 + 1 + 1
		assertEquals(4, scn.GetStrength(ted));    // 3 + 1
		assertEquals(4, scn.GetStrength(merry));  // 3 + 1
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASiteTheShadowPlayerControlsIsNotCountedAndTheBonusLastsOnlyThisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concerning = scn.GetFreepsCard("concerning");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(pippin, merry);
		scn.MoveCardsToHand(concerning);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("bree"));
		scn.MoveMinionsToTable(savage, explorer);
		assertInHand(concerning);
		scn.FreepsPass();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("valley"));

		// Bag End and Derndingle, behind both markers, go to the Shadow player's support area: no longer on the
		// adventure path (CR 4.2 "control").
		var bagEnd = scn.GetSite(1);
		var derndingle = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(bagEnd));
		assertTrue(scn.IsSiteControlled(derndingle));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{merry, explorer});
		scn.FreepsResolveSkirmish(pippin);

		// Left on the path: Rivendell Waterfall (forest), Streets of Bree and Valley of the Silverlode (dwellings).
		// Merry's skirmish is to come.
		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsPlayCard(concerning);
		scn.FreepsChooseCard(merry);
		assertEquals(4, scn.GetStrength(merry));
		scn.FreepsChooseCard(merry);
		assertEquals(5, scn.GetStrength(merry));
		scn.FreepsChooseCard(merry);

		assertInDiscard(concerning);
		assertEquals(6, scn.GetStrength(merry)); // 3 + 1 + 1 + 1
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Gone in his own skirmish, a skirmish phase of its own.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItCannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var concerning = scn.GetFreepsCard("concerning");
		var pippin = scn.GetFreepsCard("pippin");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(concerning);
		scn.MoveMinionsToTable(savage);
		assertInHand(concerning);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(concerning));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(pippin, savage);
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(concerning));
	}
}
