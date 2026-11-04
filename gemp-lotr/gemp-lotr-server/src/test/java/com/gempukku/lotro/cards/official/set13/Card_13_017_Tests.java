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
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_017_Tests
{

	// The Fellowship path with Buckleberry Ferry 1_330 as site 2 (a river; its phases are skipped only while
	// only Hobbits are companions) and Rivendell Waterfall 1_342 as site 3 (a forest and a sanctuary; its move
	// limit text needs a ranger, and none is laid).  Site 4, Balin's Tomb 1_343, is underground: neither.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kindreds", "13_17");
					put("kindreds2", "13_17");
					put("elf", "1_53");         // Lorien Elf: Elf companion, strength 4, vitality 2; "To play, spot an Elf" is its whole text
					put("legolas", "11_21");    // Legolas, Companion of the Ring: Elf companion, strength 6; +2 only at a battleground or forest
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					// [elven] cards of five kinds for the discard pile
					put("elf2", "1_53");        // a companion
					put("cloak", "1_42");       // Elven Cloak: a possession
					put("nenya", "3_23");       // Nenya: an artifact
					put("shoulder", "1_59");    // Shoulder to Shoulder: a condition
					put("defiance", "1_37");    // Defiance: an event
					// A card of another culture for the discard pile
					put("knight2", "5_35");
					// An [elven] card of a title only the Shadow player's discard pile holds
					put("bow", "1_41");         // Elven Bow
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Ford of Bruinen 1_338 as site 3: a river and a sanctuary ("The twilight cost of the
	// first Nazgul played to Ford of Bruinen each turn is -5"), the home of Golradir (3F).  Celeborn's home is site 6,
	// Galadriel's Glade 1_351, no river and no forest, which is not laid while the fellowship is at site 3: he is at
	// no site then.  He takes part in skirmishes by One Whom Men Would Follow.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kindreds", "13_17");
					put("kindreds2", "13_17");
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally, home 3F, strength 4, vitality 3; a skirmish ability used only on purpose
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, home 6F, strength 6, vitality 3; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					// One Whom Men Would Follow: "Exert Aragorn to spot an ally.  Until the regroup phase, that ally is
					// strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_338");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KindredsEstrangedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Kindreds Estranged
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [elven] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Make a minion skirmishing your Elf at a forest or river site strength -3.
		*/

		var scn = GetScenario();

		var kindreds = scn.GetFreepsCard("kindreds");

		assertEquals("Kindreds Estranged", kindreds.getBlueprint().getTitle());
		assertNull(kindreds.getBlueprint().getSubtitle());
		assertFalse(kindreds.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, kindreds.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, kindreds.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, kindreds.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(kindreds, Timeword.SKIRMISH));
		assertEquals(2, kindreds.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingFourOtherElvenCardsItMakesAMinionSkirmishingAnElfAtARiverStrengthMinusThreeAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kindreds = scn.GetFreepsCard("kindreds");
		var kindreds2 = scn.GetFreepsCard("kindreds2");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var knight = scn.GetFreepsCard("knight");
		var elf2 = scn.GetFreepsCard("elf2");
		var cloak = scn.GetFreepsCard("cloak");
		var nenya = scn.GetFreepsCard("nenya");
		var shoulder = scn.GetFreepsCard("shoulder");
		var defiance = scn.GetFreepsCard("defiance");
		var knight2 = scn.GetFreepsCard("knight2");
		var bow = scn.GetShadowCard("bow");
		scn.MoveCompanionsToTable(elf, legolas, knight);
		scn.MoveCardsToDiscard(kindreds, elf2, cloak, nenya, shoulder, defiance, knight2);
		scn.MoveCardsToDiscard(bow);
		scn.MoveCardsToHand(kindreds2);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.DISCARD, kindreds, elf2, cloak, nenya, shoulder, defiance, knight2, bow);
		assertInZone(Zone.HAND, kindreds2);
		scn.MoveMinionsToTable(troop, troop2, troop3, savage);
		scn.SkipToAssignments();
		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, troop},
				new PhysicalCardImpl[]{legolas, troop2},
				new PhysicalCardImpl[]{knight, troop3});
		// The Uruk-hai left over goes to the Lorien Elf as well: two minions in his skirmish.
		scn.ShadowAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertInZone(Zone.DISCARD, kindreds, elf2, cloak, nenya, shoulder, defiance, knight2, bow);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(13, scn.GetStrength(troop3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(kindreds));
		scn.FreepsUseCardAction(kindreds);
		// Five other [elven] cards, of five kinds, for the four.  Not the event itself, a card of another
		// culture, or the [elven] card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf2, cloak, nenya, shoulder, defiance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(kindreds, knight2, bow));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(elf2, cloak, nenya, shoulder);
		assertInZone(Zone.REMOVED, elf2, cloak, nenya, shoulder);
		assertInZone(Zone.DISCARD, defiance, knight2, bow);

		// A minion skirmishing the Elf: the Orc and the Uruk-hai in this skirmish.  Not the Troop assigned to
		// Legolas, an Elf whose skirmish is still to come, nor the Knight's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2, troop3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop2));
		// The copy played is removed from the game; the copy in hand stays there.
		assertInZone(Zone.REMOVED, kindreds);
		assertInZone(Zone.HAND, kindreds2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the Troop is 13 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void NotPlayableFromTheDiscardPileWithOnlyThreeOtherElvenCardsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kindreds = scn.GetFreepsCard("kindreds");
		var elf = scn.GetFreepsCard("elf");
		var cloak = scn.GetFreepsCard("cloak");
		var nenya = scn.GetFreepsCard("nenya");
		var shoulder = scn.GetFreepsCard("shoulder");
		var defiance = scn.GetFreepsCard("defiance");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(elf);
		// Four [elven] cards with the event itself, and a fifth card of another culture.
		scn.MoveCardsToDiscard(kindreds, cloak, nenya, shoulder, knight2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.DISCARD, kindreds, cloak, nenya, shoulder, knight2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, troop);
		scn.FreepsResolveSkirmish(elf);

		assertInZone(Zone.DISCARD, kindreds, cloak, nenya, shoulder, knight2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(kindreds));

		// A fourth other [elven] card.
		scn.MoveCardsToDiscard(defiance);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(kindreds));
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndFromHandInASkirmishOfACompanionWhoIsNoElfItGoesToTheDiscardPileAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kindreds = scn.GetFreepsCard("kindreds");
		var kindreds2 = scn.GetFreepsCard("kindreds2");
		var elf = scn.GetFreepsCard("elf");
		var knight = scn.GetFreepsCard("knight");
		var cloak = scn.GetFreepsCard("cloak");
		var nenya = scn.GetFreepsCard("nenya");
		var shoulder = scn.GetFreepsCard("shoulder");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(elf, knight);
		scn.MoveCardsToHand(kindreds);
		// The second copy lies in the discard pile with four other [elven] cards.
		scn.MoveCardsToDiscard(kindreds2, cloak, nenya, shoulder, defiance);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		assertInZone(Zone.HAND, kindreds);
		assertInZone(Zone.DISCARD, kindreds2, cloak, nenya, shoulder, defiance);
		scn.MoveMinionsToTable(troop, troop2);

		// A Skirmish event: neither copy at the assignment-phase decision.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(kindreds));
		assertFalse(scn.FreepsActionAvailable(kindreds2));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, troop},
				new PhysicalCardImpl[]{elf, troop2});
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(kindreds));
		assertTrue(scn.FreepsActionAvailable(kindreds2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(13, scn.GetStrength(troop2));
		// The Knight, at the river, is no Elf, and the Troop waiting for the Lorien Elf is not skirmishing him:
		// the event is played for nothing, and from hand it goes to the discard pile with nothing removed.
		scn.FreepsPlayCard(kindreds);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(13, scn.GetStrength(troop2));
		assertInZone(Zone.DISCARD, kindreds, kindreds2, cloak, nenya, shoulder, defiance);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtAForestTheMinionSkirmishingTheElfIsStrengthMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kindreds = scn.GetFreepsCard("kindreds");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(kindreds);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.HAND, kindreds);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsAssignToMinions(elf, troop);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPlayCard(kindreds);
		assertEquals(10, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtASiteThatIsNeitherForestNorRiverTheMinionSkirmishingTheElfKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kindreds = scn.GetFreepsCard("kindreds");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(kindreds);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.HAND, kindreds);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		scn.FreepsAssignToMinions(elf, troop);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPlayCard(kindreds);
		assertEquals(13, scn.GetStrength(troop));
		assertInZone(Zone.DISCARD, kindreds);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAnAllyIsWeakenedOnlyWhileHisHomeSiteIsAForestOrRiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var kindreds = scn.GetFreepsCard("kindreds");
		var kindreds2 = scn.GetFreepsCard("kindreds2");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.MoveCardsToHand(follow, kindreds, kindreds2);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop, troop2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		scn.FreepsPlayCard(follow);
		scn.FreepsChooseCard(celeborn);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{golradir, troop},
				new PhysicalCardImpl[]{celeborn, troop2});

		// Golradir is at his home site, the river the fellowship is at: his minion, the only one, is 13 - 3.
		scn.FreepsResolveSkirmish(golradir);
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPlayCard(kindreds);
		assertEquals(10, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// Celeborn's home site is not laid yet: he is at no site (CR 4.2 "ally"), and the event finds no minion.
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.IsCharSkirmishingAgainst(celeborn, troop2));
		scn.FreepsPlayCard(kindreds2);
		assertEquals(13, scn.GetStrength(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
