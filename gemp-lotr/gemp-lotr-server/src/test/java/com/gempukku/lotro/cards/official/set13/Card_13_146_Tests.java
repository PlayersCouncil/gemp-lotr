package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_146_Tests
{

	// The Fellowship path with Trollshaw Forest 1_334 as site 2 (a forest: "Each time you play a possession or
	// artifact on your companion, draw a card"; nothing is played on a companion here) and Rivendell Waterfall 1_342
	// as site 3 (a forest and a sanctuary; a move-limit text for a ranger, and no ranger is laid with it).  Two
	// Shadows-block dwellings are laid on the path by MoveCardToAdventurePath as the sites after site 3.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bones", "13_146");
					put("bones2", "13_146");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home 2F; a skirmish ability used only on purpose
					// [shire] cards of five kinds for the discard pile
					put("pippin1", "1_306");    // Pippin, Friend to Frodo: a companion
					put("sword", "1_299");      // Hobbit Sword: a possession
					put("mithril", "2_105");    // Mithril-coat: an artifact
					put("keptsafe", "6_111");   // Kept Safe: a condition
					put("stout", "1_315");      // Stout and Sturdy: an event
					put("knight", "5_35");      // Gondorian Knight: a card of another culture for the discard pile
					put("sting", "1_313");      // Sting: a [shire] card of a title only the Shadow player's discard pile holds
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: the Shadow player's Hobbit, a minion; abilities used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");
					put("site3", "1_342");
					remove("site8");
					remove("site9");
					put("bree", "18_140");      // Streets of Bree: a dwelling; a bar on transferring followers
					put("valley", "11_261");    // Valley of the Silverlode: a dwelling and a river; "When your fellowship
					                            //   moves to this site, you may heal each Hobbit companion"
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Hobbiton Woods 2_116 as site 1: a forest ("Fellowship: Exert a Hobbit to play your
	// site 2", used only on purpose), the home of Farmer Maggot (1F).  Site 2, where the skirmishes are, is
	// Ettenmoors 1_331, a plains and the home of Bounder (2F).
	protected VirtualTableScenario GetWoodsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bones", "13_146");
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: Hobbit ally, home 1F, strength 2; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("rosie", "1_309");      // Rosie Cotton, Hobbiton Lass: Hobbit ally, home 1F, strength 1; "Sam is strength +1", and a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_116");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Trollshaw Forest 1_334 as site 2, where the skirmishes are: a forest ("Each time you
	// play a possession or artifact on your companion, draw a card").  Site 1, the home of Rosie Cotton (1F), is Bag
	// End 1_319, no dwelling and no forest.
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bones", "13_146");
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: Hobbit ally, home 1F, strength 2; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("rosie", "1_309");      // Rosie Cotton, Hobbiton Lass: Hobbit ally, home 1F, strength 1; "Sam is strength +1", and a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EverythingbutMyBonesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Everything but My Bones
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [shire] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Make your Hobbit at a dwelling or forest site strength +3.
		*/

		var scn = GetScenario();

		var bones = scn.GetFreepsCard("bones");

		assertEquals("Everything but My Bones", bones.getBlueprint().getTitle());
		assertNull(bones.getBlueprint().getSubtitle());
		assertFalse(bones.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bones.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, bones.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, bones.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(bones, Timeword.SKIRMISH));
		assertEquals(2, bones.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingFourOtherShireCardsItMakesYourHobbitAtAForestStrengthPlusThreeAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bones = scn.GetFreepsCard("bones");
		var bones2 = scn.GetFreepsCard("bones2");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var pippin1 = scn.GetFreepsCard("pippin1");
		var sword = scn.GetFreepsCard("sword");
		var mithril = scn.GetFreepsCard("mithril");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var stout = scn.GetFreepsCard("stout");
		var knight = scn.GetFreepsCard("knight");
		var sting = scn.GetShadowCard("sting");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToDiscard(bones, pippin1, sword, mithril, keptsafe, stout, knight);
		scn.MoveCardsToDiscard(sting);
		scn.MoveCardsToHand(bones2);

		scn.StartGame();
		assertInZone(Zone.DISCARD, bones, pippin1, sword, mithril, keptsafe, stout, knight, sting);
		assertInZone(Zone.HAND, bones2);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var ted = scn.GetShadowCard("ted");
		scn.MoveMinionsToTable(runner1, runner2, ted);

		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner1},
				new PhysicalCardImpl[]{pippin, runner2},
				new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		assertInZone(Zone.DISCARD, bones, pippin1, sword, mithril, keptsafe, stout, knight, sting);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bones));
		scn.FreepsUseCardAction(bones);
		// Five other [shire] cards, of five kinds, for the four.  Not the event itself, a card of another culture,
		// or the [shire] card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin1, sword, mithril, keptsafe, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bones, knight, sting));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(pippin1, sword, mithril, keptsafe);
		assertInZone(Zone.REMOVED, pippin1, sword, mithril, keptsafe);
		assertInZone(Zone.DISCARD, stout, knight, sting);

		// Your Hobbit at a forest: Merry in this skirmish ([shire]), Pippin assigned and waiting ([gondor]), Bounder,
		// an ally at his home site, this forest, and Frodo.  Not Aragorn, who is no Hobbit, or Ted Sandyman, the
		// other player's Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, bounder, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, ted));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		assertEquals(6, scn.GetStrength(merry));
		// The copy played is removed from the game; the copy in hand stays there.
		assertInZone(Zone.REMOVED, bones);
		assertInZone(Zone.HAND, bones2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: Merry is 3 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
	}

	@Test
	public void NotPlayableFromTheDiscardPileWithOnlyThreeOtherShireCardsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bones = scn.GetFreepsCard("bones");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var mithril = scn.GetFreepsCard("mithril");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var stout = scn.GetFreepsCard("stout");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(merry);
		// Four [shire] cards with the event itself, and a fifth card of another culture.
		scn.MoveCardsToDiscard(bones, sword, mithril, keptsafe, knight);

		scn.StartGame();
		assertInZone(Zone.DISCARD, bones, sword, mithril, keptsafe, knight);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);

		assertInZone(Zone.DISCARD, bones, sword, mithril, keptsafe, knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bones));

		// A fourth other [shire] card.
		scn.MoveCardsToDiscard(stout);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bones));
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndFromHandItGoesToTheDiscardPileWithNothingRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bones = scn.GetFreepsCard("bones");
		var bones2 = scn.GetFreepsCard("bones2");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var mithril = scn.GetFreepsCard("mithril");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var stout = scn.GetFreepsCard("stout");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(bones);
		// The second copy lies in the discard pile with four other [shire] cards.
		scn.MoveCardsToDiscard(bones2, sword, mithril, keptsafe, stout);

		scn.StartGame();
		assertInZone(Zone.HAND, bones);
		assertInZone(Zone.DISCARD, bones2, sword, mithril, keptsafe, stout);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		// A Skirmish event: neither copy at the assignment-phase decision.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bones));
		assertFalse(scn.FreepsActionAvailable(bones2));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(bones));
		assertTrue(scn.FreepsActionAvailable(bones2));
		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsPlayCard(bones);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(6, scn.GetStrength(merry));
		// From hand it goes to the discard pile, and the four [shire] cards there stay.
		assertInZone(Zone.DISCARD, bones, bones2, sword, mithril, keptsafe, stout);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtAForestThatIsASanctuaryAndAtADwellingYourHobbitIsStrengthPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bones = scn.GetFreepsCard("bones");
		var bones2 = scn.GetFreepsCard("bones2");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(bones, bones2);

		scn.StartGame();
		assertInZone(Zone.HAND, bones, bones2);
		scn.SkipToSite(2);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		// Rivendell Waterfall: a forest that is also a sanctuary.
		scn.SkipToAssignments();
		assertSame(scn.GetFreepsSite("site3").getBlueprint(), scn.GetCurrentSite().getBlueprint());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsPlayCard(bones);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(6, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Streets of Bree, a dwelling, by a second move.
		scn.MoveCardToAdventurePath(bree);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertSame(bree, scn.GetCurrentSite());
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner2);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsPlayCard(bones2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(6, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtADwellingThatIsARiverYourHobbitIsStrengthPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bones = scn.GetFreepsCard("bones");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		var bree = scn.GetShadowSite("bree");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(bones);

		scn.StartGame();
		assertInZone(Zone.HAND, bones);
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(bree);
		scn.FreepsPass();
		assertSame(bree, scn.GetCurrentSite());

		// Valley of the Silverlode, a dwelling that is also a river, by a second move.  Nobody is wounded, so its
		// heal is not offered.
		scn.MoveCardToAdventurePath(valley);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertSame(valley, scn.GetCurrentSite());
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsPlayCard(bones);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(6, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoHobbitAtADwellingOrForestTheEventIsStillPlayedAndChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoodsScenario();

		var bones = scn.GetFreepsCard("bones");
		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(bones);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.HAND, bones);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		// Ettenmoors, a plains, with Bounder at his home site there: no Hobbit is at a dwelling or a forest.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.DWELLING));
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);
		assertEquals(2, scn.GetStrength(bounder));

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(bones));
		scn.FreepsPlayCard(bones);
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(bounder));
		assertInZone(Zone.DISCARD, bones);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AlliesAtTheirHomeSiteAForestAreChosenWithTheFellowshipAtAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoodsScenario();

		var bones = scn.GetFreepsCard("bones");
		var frodo = scn.GetRingBearer();
		var maggot = scn.GetFreepsCard("maggot");
		var bounder = scn.GetFreepsCard("bounder");
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveAlliesToTable(maggot, bounder, rosie);
		scn.MoveCardsToHand(bones);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(1), Keyword.FOREST));
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);
		int bounderStrength = scn.GetStrength(bounder);
		assertEquals(2, scn.GetStrength(maggot));
		assertEquals(1, scn.GetStrength(rosie));

		// Farmer Maggot and Rosie Cotton are at their home site, Hobbiton Woods (CR 4.2 "ally"), a forest though the
		// fellowship is not at one.  Frodo is at Ettenmoors with the fellowship, and Bounder at his home site,
		// Ettenmoors.
		assertTrue(scn.FreepsPlayAvailable(bones));
		scn.FreepsPlayCard(bones);
		assertTrue(scn.FreepsHasCardChoiceAvailable(maggot, rosie));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bounder));
		scn.FreepsChooseCard(maggot);
		assertEquals(5, scn.GetStrength(maggot));
		assertEquals(1, scn.GetStrength(rosie));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(bounderStrength, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWhoseHomeIsNoForestIsNotChosenWithTheFellowshipAtAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var bones = scn.GetFreepsCard("bones");
		var frodo = scn.GetRingBearer();
		var rosie = scn.GetFreepsCard("rosie");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(rosie, bounder);
		scn.MoveCardsToHand(bones);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Trollshaw Forest", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);

		// Frodo is at the forest with the fellowship, and Bounder at his home site, Trollshaw Forest (CR 4.2
		// "ally"); Rosie at her home site, Bag End.
		int bounderStrength = scn.GetStrength(bounder);
		scn.FreepsPlayCard(bones);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rosie));
		scn.FreepsChooseCard(frodo);
		assertEquals(frodoStrength + 3, scn.GetStrength(frodo));
		assertEquals(bounderStrength, scn.GetStrength(bounder));
		assertEquals(1, scn.GetStrength(rosie));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
