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

public class Card_10_116_Tests
{

	// The Ring is Answer To All Riddles: "Skirmish: Add a burden to wear The One Ring until the regroup phase",
	// which is what Southern Spies answers.  It adds no strength while it is not worn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale", "10_116");
					put("sam", "1_311");        // Sam, Son of Hamfast: Hobbit, strength 3, vitality 4; a fellowship ability only
					// Pippin, Trollslayer: a Hobbit who is [gondor] and not [shire]; his skirmish ability is about a minion he is skirmishing
					put("pippin", "103_31");
					// Smeagol, Once Like Us: a [shire] companion with no race; his assignment ability needs a fellowship of [shire] companions only
					put("smeagol", "103_116");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a companion who is no Hobbit
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
					// Seek and Hide, a Free Peoples skirmish event: "Spot 2 [shire] companions to make a minion lose all damage bonuses."
					put("seek", "3_112");

					// White Hand Destroyer: strength 10, vitality 3, fierce; hunter only for sites its owner controls
					put("destroyer", "17_124");
					// Isengard Sword, on an [uruk-hai] minion: "Each character skirmishing bearer is strength -2."
					put("sword", "11_192");
					// Saruman's Frost, on a site: "Each Hobbit at this site is strength -2."
					put("frost", "1_135");
					put("patrol", "1_177");     // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					// Army of Haradrim: a Southron, strength 14; "Skirmish: Remove (3) to play a [raider] mount from your discard pile."
					put("army", "5_70");
					// War Mumak, a [raider] mount on a Southron, cost 3, strength +4: bearer is fierce, and each time
					// bearer overwhelms a character its owner may add (5)
					put("mumak", "5_78");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					// Ted Sandyman: a [men] minion who is a Hobbit, strength 3; a maneuver ability that needs a site he controls
					put("ted", "18_75");
					// Sauron, The Lord of the Rings: a [sauron] minion, strength 24, damage +2, fierce
					put("sauron", "9_48");

					// Three Shadow events.  Enduring Evil ([sauron], Skirmish): "Spot X burdens to make a character
					// skirmishing a [sauron] Orc strength -X."
					put("evil", "1_246");
					put("drums", "1_168");      // Drums in the Deep ([moria], Skirmish): "Make a [moria] Orc strength +2 (or +4 if skirmishing a Dwarf)."
					// Savagery to Match Their Numbers ([isengard], Skirmish): "Make an Uruk-hai strength +2", or more with 5 companions
					put("savagery", "1_139");
					// Southern Spies ([sauron], Response): "If the Ring-bearer puts on The One Ring, spot a [sauron]
					// minion to make the Free Peoples player discard his or her hand."
					put("spies", "2_91");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void TheTaleoftheGreatRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: The Tale of the Great Ring
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Tale</b>.
		 * <b>Skirmish:</b> Prevent a Hobbit from being overwhelmed unless a Shadow event is (or was) played during this skirmish. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tale");

		assertEquals("The Tale of the Great Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForNothingToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToHand(tale);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(tale));

		scn.FreepsPlayCard(tale);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, tale);
		assertNull(tale.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PreventsAHobbitFromBeingOverwhelmedForThatSkirmishOnlyAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToSupportArea(tale);
		scn.MoveCompanionsToTable(sam);

		var destroyer = scn.GetShadowCard("destroyer");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer);

		// A skirmish ability: not on offer in the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tale));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(sam, destroyer);
		assertTrue(scn.FreepsActionAvailable(tale));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(10, scn.GetStrength(destroyer));

		scn.FreepsUseCardAction(tale);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.DISCARD, tale);

		// Sam, 3, loses to the Destroyer, 10, and is not overwhelmed: he takes the one wound of losing.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		// The Destroyer is fierce.  The second skirmish is another skirmish, and there Sam is overwhelmed.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(sam, destroyer);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(10, scn.GetStrength(destroyer));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyHobbitMayBeChosenAndOneWhoIsNotFightingLeavesTheFighterToBeOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToSupportArea(tale);
		scn.MoveCompanionsToTable(sam, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		var destroyer = scn.GetShadowCard("destroyer");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer, ted);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, destroyer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsUseCardAction(tale);
		// A Hobbit of any culture, type or side, fighting or not: Sam, Frodo, Pippin of [gondor], the ally and
		// the Shadow player's Ted Sandyman.  Not Smeagol, a [shire] companion who is no Hobbit, nor Aragorn,
		// nor the minion Sam is fighting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, destroyer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(pippin);
		assertInZone(Zone.DISCARD, tale);

		// It was Pippin who was protected: Sam, 3 against 10, is overwhelmed.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DEAD, sam);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		// The Destroyer is fierce: its second assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AShadowEventPlayedBeforeItInThatSkirmishLeavesItNothingToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToSupportArea(tale);
		scn.MoveCompanionsToTable(sam);

		var patrol = scn.GetShadowCard("patrol");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, patrol);
		scn.FreepsPass();
		assertEquals(13, scn.GetStrength(patrol));
		scn.ShadowPlayCard(drums);
		assertEquals(15, scn.GetStrength(patrol));
		assertInZone(Zone.DISCARD, drums);

		// The Tale may still be used, and is discarded for it.
		assertTrue(scn.FreepsActionAvailable(tale));
		scn.FreepsUseCardAction(tale);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo));
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.DISCARD, tale);

		// A Shadow event was played during this skirmish: Sam is overwhelmed.  (Not overwhelmed, he would have
		// taken one wound: the Patrol Troop has no damage bonus.)
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AShadowEventPlayedAfterItInThatSkirmishEndsThePrevention() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToSupportArea(tale);
		scn.MoveCompanionsToTable(sam);

		var sauron = scn.GetShadowCard("sauron");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(sauron);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, sauron);

		scn.FreepsUseCardAction(tale);
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.DISCARD, tale);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(24, scn.GetStrength(sauron));

		// Enduring Evil spots the burden.  Sauron is no Orc, so it weakens nobody: it is played all the same.
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);
		scn.ShadowChoose("1");
		assertInZone(Zone.DISCARD, evil);
		assertEquals(3, scn.GetStrength(sam));

		// Sam is overwhelmed.  (Not overwhelmed, he would have lived through the 3 wounds of Sauron's win.)
		scn.FreepsPass();
		scn.ShadowPass();
		assertInZone(Zone.DEAD, sam);
		// Sauron is fierce: his second assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AShadowResponseEventIsAShadowEventToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var tale = scn.GetFreepsCard("tale");
		var seek = scn.GetFreepsCard("seek");
		scn.MoveCardsToSupportArea(tale);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(seek);

		var sauron = scn.GetShadowCard("sauron");
		var spies = scn.GetShadowCard("spies");
		scn.MoveCardsToHand(spies);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, sauron);

		scn.FreepsUseCardAction(tale);
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.DISCARD, tale);
		scn.ShadowPass();

		// Frodo puts on the Ring in Sam's skirmish, and the Shadow player answers with Southern Spies.
		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(ring);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(spies));
		scn.ShadowPlayCard(spies);
		assertInZone(Zone.DISCARD, spies);
		assertInZone(Zone.DISCARD, seek);

		// Sam is overwhelmed.  (Not overwhelmed, he would have lived through the 3 wounds of Sauron's win.)
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DEAD, sam);
		// Sauron is fierce: his second assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AShadowEventOfAnEarlierSkirmishAShadowCardThatIsNoEventAndAFreePeoplesEventDoNotEndIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var tale = scn.GetFreepsCard("tale");
		var seek = scn.GetFreepsCard("seek");
		scn.MoveCardsToSupportArea(tale);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(seek);

		var army = scn.GetShadowCard("army");
		var mumak = scn.GetShadowCard("mumak");
		var savage = scn.GetShadowCard("savage");
		var savagery = scn.GetShadowCard("savagery");
		scn.MoveCardsToHand(savagery);
		scn.MoveCardsToDiscard(mumak);

		scn.StartGame();
		scn.MoveMinionsToTable(army, savage);
		// Enough for the Army's ability and its mount.
		scn.SetTwilight(10);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{sam, army});

		// Aragorn's skirmish comes first, and the Shadow player plays an event in it: the Savage, the only
		// Uruk-hai, is 5 + 2, and still loses to Aragorn's 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPlayCard(savagery);
		assertEquals(7, scn.GetStrength(savage));
		assertInZone(Zone.DISCARD, savagery);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));

		// Sam's skirmish.
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsUseCardAction(tale);
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.DISCARD, tale);

		// A Shadow card played in it that is no event: the Army's ability plays its mount from the discard pile.
		int twilight = scn.GetTwilight();
		assertEquals(14, scn.GetStrength(army));
		scn.ShadowUseCardAction(army);
		assertAttachedTo(mumak, army);
		assertEquals(14 + 4, scn.GetStrength(army));
		// 3 for the ability + 3 for the War Mumak
		assertEquals(twilight - 3 - 3, scn.GetTwilight());

		// A Free Peoples event played in it is no Shadow event: the Savage loses its damage +1.
		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
		scn.FreepsPlayCard(seek);
		scn.FreepsChooseCard(savage);
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, seek);

		// Sam, 3, loses to the Army, 18, and is not overwhelmed: one wound, and the War Mumak has no overwhelm
		// to answer.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// The Army is fierce on its mount: its second assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PreventsTheOverwhelmOfAHobbitWhoseStrengthIsZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToSupportArea(tale);
		scn.MoveCompanionsToTable(sam);

		var destroyer = scn.GetShadowCard("destroyer");
		var sword = scn.GetShadowCard("sword");
		var frost = scn.GetShadowCard("frost");

		scn.StartGame();
		scn.FreepsPass();
		scn.AttachCardsTo(scn.GetCurrentSite(), frost);
		scn.MoveMinionsToTable(destroyer);
		scn.AttachCardsTo(destroyer, sword);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, destroyer);
		// 3 - 2 for Saruman's Frost - 2 for the Isengard Sword, and no less than 0
		assertEquals(0, scn.GetStrength(sam));
		assertEquals(10, scn.GetStrength(destroyer));

		scn.FreepsUseCardAction(tale);
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.DISCARD, tale);

		// A side of no strength at all is overwhelmed by any side that has some; Sam is not, and takes the one
		// wound of losing.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		// The Destroyer is fierce: its second assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
