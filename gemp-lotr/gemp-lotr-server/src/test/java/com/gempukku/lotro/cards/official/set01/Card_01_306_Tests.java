package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_306_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "1_306");

					// [shire] tales.  There and Back Again: a condition a Hobbit companion bears; "Maneuver: Discard
					// this condition to make each Hobbit companion strength +2 until the regroup phase."
					put("taba", "1_317");
					// Stone Trolls: a condition any character bears ("To play, spot Bilbo": cheated on); strength
					// against a Troll or an Uruk-hai only
					put("trolls", "1_314");
					// Three Monstrous Trolls: a condition in the support area; its response needs a Hobbit ally
					put("monstrous", "3_114");
					// Red Book of Westmarch: a possession in the support area; its trigger needs Bilbo and a tale played
					put("redbook", "2_113");

					// The Saga of Elendil: a [gondor] tale, a condition a [gondor] companion bears (vitality +1)
					put("saga", "1_114");
					// Thorin's Harp: a [dwarven] tale in the support area; its ability needs a [dwarven] companion
					put("harp", "18_3");
					// The Shire Countryside: a [shire] condition that is no tale; it answers a burden removed
					put("countryside", "3_113");
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, a weapon and no tale
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: an ally; a fellowship ability only

					put("uruk", "1_151");       // Uruk Savage: an [isengard] minion, vitality 3; Damage +1 is its whole text
					put("power", "1_136");      // Saruman's Power: "Shadow: Exert an [isengard] minion to discard all conditions."
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: vitality 4; fierce, and strength for other Nazgul
					put("dumb", "2_80");        // Stricken Dumb: "Maneuver: Exert The Witch-king to discard a tale or weapon."
					put("soldier", "1_271");    // Orc Soldier: a [sauron] minion, vitality 2; a skirmish ability only
					// Why Shouldn't I Keep It?: "Maneuver: Spot a [sauron] minion to discard Bilbo. The Free Peoples
					// player may discard 2 Free Peoples conditions to prevent this."
					put("keepit", "3_105");
					// Massing in the East: "Regroup: Exert a [sauron] minion to discard a card from the top of the Free
					// Peoples player's draw deck for each of these races you can spot in the fellowship: Dwarf, Elf,
					// Man, and Wizard."
					put("massing", "3_92");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Buckland Homestead, a site of the Shadows block, is laid as site 2: "Shadow: Play a Nazgul to discard a
	// condition from play."
	protected VirtualTableScenario GetHomesteadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "1_306");
					put("taba", "1_317");         // There and Back Again: a [shire] tale a Hobbit companion bears
					put("monstrous", "3_114");    // Three Monstrous Trolls: a [shire] tale in the support area
					put("harp", "18_3");          // Thorin's Harp: a [dwarven] tale in the support area
					put("countryside", "3_113");  // The Shire Countryside: a [shire] condition that is no tale
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul, cost 4, site number 3; her text needs a [wraith]
					// condition on a companion
					put("lemenya", "1_232");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("homestead", "11_230");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pippin, Friend to Frodo
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Your opponent may not discard your [shire] tales from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Friend to Frodo", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}

	@Test
	public void IsPlayedFromHandForOneToTheFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(pippin);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayersStrickenDumbMayChooseNoShireTaleOfHisOwnersButATaleOfAnotherCultureOrAShireCardThatIsNoTale() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var trolls = scn.GetFreepsCard("trolls");
		var monstrous = scn.GetFreepsCard("monstrous");
		var redbook = scn.GetFreepsCard("redbook");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var witchking = scn.GetShadowCard("witchking");
		var dumb = scn.GetShadowCard("dumb");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.AttachCardsTo(aragorn, trolls, saga);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(monstrous, redbook);
		scn.MoveCardsToHand(dumb);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(dumb));

		// "Discard a tale or weapon."
		scn.ShadowPlayCard(dumb);
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertEquals(1, scn.ShadowGetChoiceMin());
		// A [gondor] tale, and a [shire] card that is no tale
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, sword));
		// [shire] tales: a condition a companion bears, a condition in the support area, a possession
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(trolls, monstrous, redbook));

		scn.ShadowChooseCard(saga);
		assertInZone(Zone.DISCARD, saga);
		assertAttachedTo(trolls, aragorn);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.SUPPORT, monstrous, redbook);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutPippinTheSameShireTalesMayBeChosenAndTheOneChosenIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var trolls = scn.GetFreepsCard("trolls");
		var monstrous = scn.GetFreepsCard("monstrous");
		var redbook = scn.GetFreepsCard("redbook");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var witchking = scn.GetShadowCard("witchking");
		var dumb = scn.GetShadowCard("dumb");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, trolls, saga);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(monstrous, redbook);
		scn.MoveCardsToHand(dumb);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPlayCard(dumb);
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, sword, trolls, monstrous, redbook));

		scn.ShadowChooseCard(redbook);
		assertInZone(Zone.DISCARD, redbook);
		assertAttachedTo(trolls, aragorn);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, monstrous);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SarumansPowerDiscardsEveryConditionButHisOwnersShireTales() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var taba = scn.GetFreepsCard("taba");
		var monstrous = scn.GetFreepsCard("monstrous");
		var countryside = scn.GetFreepsCard("countryside");
		var harp = scn.GetFreepsCard("harp");
		var uruk = scn.GetShadowCard("uruk");
		var power = scn.GetShadowCard("power");
		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(frodo, taba);
		scn.MoveCardsToSupportArea(monstrous, countryside, harp);
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(taba, frodo);
		assertInZone(Zone.SUPPORT, monstrous, countryside, harp);

		// "Discard all conditions."
		scn.ShadowPlayCard(power);
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertAttachedTo(taba, frodo);
		assertInZone(Zone.SUPPORT, monstrous);
		// A [shire] condition that is no tale, and a [dwarven] tale
		assertInZone(Zone.DISCARD, countryside, harp);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerDiscardingByASitesTextMayNotChooseHisOwnersShireTales() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHomesteadScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var taba = scn.GetFreepsCard("taba");
		var monstrous = scn.GetFreepsCard("monstrous");
		var countryside = scn.GetFreepsCard("countryside");
		var harp = scn.GetFreepsCard("harp");
		var lemenya = scn.GetShadowCard("lemenya");
		var homestead = scn.GetShadowSite("homestead");
		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(frodo, taba);
		scn.MoveCardsToSupportArea(monstrous, countryside, harp);
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveCardToAdventurePath(homestead);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(homestead, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(homestead));

		// The discard is the Shadow player's own, by a card that is of neither side.
		scn.ShadowUseCardAction(homestead);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(countryside, harp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(taba, monstrous));

		scn.ShadowChooseCard(harp);
		assertInZone(Zone.DISCARD, harp);
		assertAttachedTo(taba, frodo);
		assertInZone(Zone.SUPPORT, monstrous, countryside);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PippinDoesNotBlockFreepsUsingThereAndBackAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var taba = scn.GetFreepsCard("taba");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(frodo, taba);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(taba));
		assertAttachedTo(taba, frodo);
		assertEquals(3, scn.GetStrength(pippin));

		// His owner discards his own tale, as the cost of its own ability.
		scn.FreepsUseCardAction(taba);
		assertInZone(Zone.DISCARD, taba);
		// 3 + 2
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void HisOwnerMayDiscardHisShireTalesToPreventAShadowEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var taba = scn.GetFreepsCard("taba");
		var monstrous = scn.GetFreepsCard("monstrous");
		var countryside = scn.GetFreepsCard("countryside");
		var soldier = scn.GetShadowCard("soldier");
		var keepit = scn.GetShadowCard("keepit");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(frodo, taba);
		scn.MoveCardsToSupportArea(monstrous, countryside);
		scn.MoveCardsToHand(keepit);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// "Discard Bilbo. The Free Peoples player may discard 2 Free Peoples conditions to prevent this."
		scn.ShadowPlayCard(keepit);
		scn.FreepsChooseYes();
		// The one discarding is the player who chooses to: his own tales are his to give up.
		assertTrue(scn.FreepsHasCardChoiceAvailable(taba, monstrous, countryside));
		scn.FreepsChooseCards(taba, monstrous);

		assertInZone(Zone.DISCARD, taba, monstrous);
		assertInZone(Zone.SUPPORT, bilbo, countryside);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShireTaleOnTopOfHisOwnersDrawDeckIsDiscardedByAShadowEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var monstrous = scn.GetFreepsCard("monstrous");
		var soldier = scn.GetShadowCard("soldier");
		var massing = scn.GetShadowCard("massing");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(monstrous);
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(3);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertSame(monstrous, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();

		// Aragorn is the one Man, Dwarf, Elf or Wizard in the fellowship: one card, and it is not in play.
		scn.ShadowPlayCard(massing);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInZone(Zone.DISCARD, monstrous);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
