package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_022_Tests
{

	// The Fellowship path with Breeland Forest 1_329 as site 2 (a forest) and Ford of Bruinen 1_338 as site 3 (a
	// river and a sanctuary); site 4 Balin's Tomb is underground, and site 1 Bag End has no terrain.  Anduin
	// Wilderland 1_354 (Forest, River) stands as site 7, to be laid after the current site by MoveCardToAdventurePath;
	// site 7 is never reached.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_22");

					put("lorienelf", "1_53");   // Lórien Elf: Elf companion; to play, spot an Elf; no other text
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: Elf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose

					put("agility", "6_12");     // Agility: another [elven] support-area condition; "When you play this condition, you may spot an Elf to place 2 [elven] tokens here"; a skirmish ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text answers only its own play

					// Plain cards for the draw deck and the hands: Elf companions with no text but "To play, spot an Elf"
					put("filler1", "1_53");
					put("filler2", "1_53");
					put("filler3", "1_53");
					put("filler4", "1_53");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_329");      // Breeland Forest: Forest; stealth events may not be played
					put("site3", "1_338");      // Ford of Bruinen: River, Sanctuary; a Nazgûl discount only
					put("site7", "1_354");      // Anduin Wilderland: Forest, River; skips the archery phase while the fellowship is there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path the other way round for the sanctuary: Buckleberry Ferry 1_330 as site 2 (a river and no
	// sanctuary) and Rivendell Waterfall 1_342 as site 3 (a forest and a sanctuary).
	protected VirtualTableScenario GetSanctuaryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_22");

					put("lorienelf", "1_53");   // Lórien Elf: Elf companion; to play, spot an Elf; no other text
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: Elf ally; a fellowship ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");      // Buckleberry Ferry: River; no assignment and skirmish phases while only Hobbits are in the fellowship
					put("site3", "1_342");      // Rivendell Waterfall: Forest, Sanctuary; a move-limit bonus only while a ranger is spotted there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SecludedHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Secluded Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 Elves.<br>When you play this, add an [elven] token here for each forest site and each river site on the adventure path.<br><b>Regroup:</b> Discard this from play or remove 2 tokens from here to reconcile your hand.
		*/

		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");

		assertEquals("Secluded Homestead", homestead.getBlueprint().getTitle());
		assertNull(homestead.getBlueprint().getSubtitle());
		assertTrue(homestead.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, homestead.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, homestead.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, homestead.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(homestead, Keyword.SUPPORT_AREA));
		assertEquals(2, homestead.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayableOnceASecondElfIsSpottedAndTakesATokenForTheForestAndTheRiverOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var agility = scn.GetFreepsCard("agility");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(agility);
		scn.AddTokensToCard(agility, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(agility));
		scn.SkipToSite(4);
		scn.MoveCardsToHand(homestead, lorienelf);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.RIVER));
		assertTrue(scn.HasKeyword(scn.GetSite(4), Keyword.UNDERGROUND));

		// Celeborn, an Elf ally, and Aragorn, a companion who is no Elf: one Elf.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(homestead));

		// A second Elf, a companion, is played.
		scn.FreepsPlayCard(lorienelf);
		assertInZone(Zone.FREE_CHARACTERS, lorienelf);
		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);

		assertInZone(Zone.SUPPORT, homestead);
		// Breeland Forest and Ford of Bruinen; not Balin's Tomb or Bag End.
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(agility));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheForestItTakesOneTokenAndNoneForTheRiverNotYetOnThePathNorForAnotherCardPlayedAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var agility = scn.GetFreepsCard("agility");
		scn.MoveCompanionsToTable(lorienelf);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(homestead, agility);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.FOREST));
		// Ford of Bruinen, a river, is not yet on the path.
		assertNull(scn.GetSite(3));

		scn.FreepsPlayCard(homestead);
		assertEquals(1, scn.GetCultureTokensOn(homestead));

		// Another [elven] condition played after it puts its own tokens on itself, and none here.
		scn.FreepsPlayCard(agility);
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.SUPPORT, agility);
		assertEquals(2, scn.GetCultureTokensOn(agility));
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AControlledForestSiteIsNoLongerOnThePathAndAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(lorienelf);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.SkipToSite(4);
		var forest = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(forest);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		// Breeland Forest is still a forest, but a controlled site is in its controller's support area.
		assertTrue(scn.HasKeyword(forest, Keyword.FOREST));
		assertInZone(Zone.SUPPORT, forest);
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.RIVER));

		scn.FreepsPlayCard(homestead);
		// Ford of Bruinen alone; not the controlled Breeland Forest.
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARiverCountsWithoutBeingASanctuaryAndAForestCountsBeingOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSanctuaryScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(lorienelf);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.RIVER));
		assertFalse(scn.HasKeyword(scn.GetSite(2), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.SANCTUARY));
		assertEquals(0, scn.GetCultureTokensOn(homestead));

		scn.FreepsPlayCard(homestead);
		// Buckleberry Ferry and Rivendell Waterfall; not Balin's Tomb or Bag End.
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Q-0383: A (Ketura, 2026-10-28): a site that is both a forest and a river adds a token for each word.
	@Test
	public void ASiteThatIsBothAForestAndARiverAddsATokenForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var wilderland = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable(lorienelf);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardToAdventurePath(wilderland);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertEquals(wilderland, scn.GetSite(5));
		assertTrue(scn.HasKeyword(wilderland, Keyword.FOREST));
		assertTrue(scn.HasKeyword(wilderland, Keyword.RIVER));

		scn.FreepsPlayCard(homestead);
		// Breeland Forest 1 + Ford of Bruinen 1 + Anduin Wilderland, a forest site 1 and a river site 1
		assertEquals(4, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesTwoTokensOrDiscardsItToReconcileTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var agility = scn.GetFreepsCard("agility");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		scn.MoveCardsToSupportArea(homestead, agility);
		scn.AddTokensToCard(homestead, 3);
		scn.AddTokensToCard(agility, 2);
		scn.MoveCardsToHand(filler1, filler2);

		var shadow1 = scn.GetShadowCard("filler1");
		var shadow2 = scn.GetShadowCard("filler2");
		scn.MoveCardsToHand(shadow1, shadow2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(agility));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		scn.SkipToPhase(Phase.REGROUP);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		// Both costs can be paid: the player is asked which.
		assertEquals(List.of("Discard this from play", "Remove 2 tokens from here"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove 2 tokens from here");
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);

		// The reconcile: the optional discard declined, then drawn up to 8.
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());

		// With one token left only the discard can be paid, and it is paid unasked; the tokens on the other
		// [elven] condition are not "from here".  With 8 in hand the reconcile's one discard is still offered.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		assertInDiscard(homestead);
		assertEquals(2, scn.GetCultureTokensOn(agility));
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(homestead));
		assertEquals(3, scn.GetCultureTokensOn(homestead));
	}
}
