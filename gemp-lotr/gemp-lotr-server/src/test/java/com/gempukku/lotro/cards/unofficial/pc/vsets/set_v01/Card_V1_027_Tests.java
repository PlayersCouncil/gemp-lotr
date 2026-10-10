package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_027_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frostbite", "101_27");
					put("frostbite2", "101_27");
					put("frostbite3", "101_27");
					put("frostbite4", "101_27");
					// Urgency: [isengard] weather condition, cost 0, nothing needed to play it, support area.  It
					// adds twilight on a move for each Ent (there are none) and has a skirmish ability.
					put("urgency", "19_17");
					// Saruman's Chill: [isengard] weather condition, cost 1.  To play, exert an [isengard]
					// minion; plays on a site.  Hobbits moving from that site exert; discarded at end of turn.
					put("chill", "1_134");
					// Cruel Caradhras: [isengard] weather event, maneuver, cost 2.  Exert an [isengard] minion
					// to make the Free Peoples player exert the Ring-bearer or add a burden.
					put("caradhras", "1_124");
					// Saruman's Ambition: [isengard] condition that is not weather, cost 0, support area.  It
					// makes [isengard] events cost 1 less and is kept out of the test that plays one.
					put("ambition", "1_133");
					// Saruman, Black Traitor: cost 4, site 4.  "When you play Saruman, you may discard a condition."
					put("saruman", "4_173");
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, strength 5, vitality 3; damage +1 is its whole text

					// Aragorn, Ranger of the North: a Man; a maneuver ability never used here
					put("aragorn", "1_89");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability never used here
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, vitality 2, no text in play
					put("merry", "1_303");      // Merry, From O'er the Brandywine: Hobbit companion; stronger with a weapon, and has none
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally; a maneuver ability never used here
					// Seven We Had: [shire] fellowship event, cost 0.  Add a burden to discard a condition borne
					// by a Hobbit.
					put("seven", "4_318");
					// Make Light of Burdens: [dwarven] condition, support area.  Maneuver: exert a Dwarf
					// companion and discard the top 3 cards of your draw deck to discard a Shadow condition
					// from a Dwarf (or a weather condition).
					put("burdens", "2_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHeightsOfIsengardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frostbite", "101_27");
					put("frostbite2", "101_27");
					// Gandalf, The Grey Wizard: [gandalf] companion, strength 7, vitality 4; a fellowship
					// ability never used here
					put("gandalf", "1_364");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					// Fangorn Glade: adds twilight for Free Peoples weapons when the fellowship moves to it
					put("site1", "11_239");
					// Heights of Isengard: "Each time a [gandalf] companion wins a skirmish, the Free Peoples
					// player may discard a condition from play."
					put("site2", "11_244");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void FrostbiteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Frostbite
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: To play, spot a weather condition.  Plays on a companion. Limit 1 per bearer.
		 * Each time a weather card is played, add (2).
		 * When this condition is discarded by a Free Peoples card, wound bearer.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("frostbite");

		assertEquals("Frostbite", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		// It speaks of weather and is not itself a weather card.
		assertFalse(scn.HasKeyword(card, Keyword.WEATHER));
	}

	@Test
	public void CannotBePlayedUntilAWeatherConditionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frostbite = scn.GetShadowCard("frostbite");
		var urgency = scn.GetShadowCard("urgency");
		var chill = scn.GetShadowCard("chill");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToHand(frostbite, urgency);
		scn.MoveCardsToSupportArea(ambition);
		scn.MoveCardsToDiscard(chill);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// In play: an [isengard] condition that is not weather.  One weather condition is in hand and another is
		// in the discard pile.
		assertInPlay(ambition);
		assertInZone(Zone.HAND, urgency);
		assertInZone(Zone.DISCARD, chill);
		assertFalse(scn.ShadowPlayAvailable(frostbite));

		scn.ShadowPlayCard(urgency);

		assertInPlay(urgency);
		assertTrue(scn.ShadowPlayAvailable(frostbite));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysForOneTwilightOnAChosenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var frostbite = scn.GetShadowCard("frostbite");
		var urgency = scn.GetShadowCard("urgency");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		// The weather condition spotted: Urgency, in the support area.  With no Ent in play it adds nothing
		// when the fellowship moves.
		scn.MoveCardsToSupportArea(urgency);
		scn.MoveCardsToHand(frostbite);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 3 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertInPlay(urgency);
		assertTrue(scn.HasKeyword(urgency, Keyword.WEATHER));
		assertInZone(Zone.HAND, frostbite);

		scn.ShadowPlayCard(frostbite);

		// The Ring-bearer and a Man, and neither an ally nor a minion
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thrarin, savage));
		scn.ShadowChooseCard(aragorn);

		// Frostbite is not a weather card, so playing it adds nothing: 7 - 1
		assertEquals(6, scn.GetTwilight());
		assertAttachedTo(frostbite, aragorn);
		assertNotAttachedTo(frostbite, frodo);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CompanionWhoBearsOneIsNotOfferedAsBearerOfASecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var frostbite = scn.GetShadowCard("frostbite");
		var frostbite2 = scn.GetShadowCard("frostbite2");
		var frostbite3 = scn.GetShadowCard("frostbite3");
		var frostbite4 = scn.GetShadowCard("frostbite4");
		var urgency = scn.GetShadowCard("urgency");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, frostbite);
		scn.MoveCardsToSupportArea(urgency);
		scn.MoveCardsToHand(frostbite2, frostbite3, frostbite4);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 3 set + 3 companions + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertAttachedTo(frostbite, aragorn);
		assertTrue(scn.ShadowPlayAvailable(frostbite2));

		scn.ShadowPlayCard(frostbite2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(gimli);
		assertAttachedTo(frostbite2, gimli);

		// Frodo is the only companion left without one, so the third goes to him without a choice.
		assertTrue(scn.ShadowPlayAvailable(frostbite3));
		scn.ShadowPlayCard(frostbite3);
		assertAttachedTo(frostbite3, frodo);

		// Every companion bears one now, and there is a fourth in hand and twilight to pay for it: 8 - 2
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.HAND, frostbite4);
		assertFalse(scn.ShadowPlayAvailable(frostbite4));
		assertEquals(1, scn.GetAttachedCards(aragorn).size());
		assertEquals(1, scn.GetAttachedCards(gimli).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWeatherConditionPlayedAddsTwoTwilightAndOtherCardsAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var frostbite = scn.GetShadowCard("frostbite");
		var frostbite2 = scn.GetShadowCard("frostbite2");
		var urgency = scn.GetShadowCard("urgency");
		var chill = scn.GetShadowCard("chill");
		var ambition = scn.GetShadowCard("ambition");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, frostbite);
		scn.MoveCardsToHand(urgency, chill, ambition, frostbite2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 4 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());

		// A weather condition that costs nothing: 8 + 2
		scn.ShadowPlayCard(urgency);
		assertInPlay(urgency);
		assertEquals(10, scn.GetTwilight());

		// A second weather condition, played on a site at the cost of exerting the Uruk Savage: 10 - 1 + 2
		scn.ShadowPlayCard(chill);
		scn.ShadowChooseCard(scn.GetCurrentSite());
		assertAttachedTo(chill, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(11, scn.GetTwilight());

		// An [isengard] condition that is not weather and costs nothing: nothing is added.
		scn.ShadowPlayCard(ambition);
		assertInPlay(ambition);
		assertEquals(11, scn.GetTwilight());

		// Another Frostbite, which goes to Frodo as the only companion without one: 11 - 1, and nothing added.
		scn.ShadowPlayCard(frostbite2);
		assertAttachedTo(frostbite2, frodo);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WeatherEventAddsTwoTwilightForEachFrostbiteInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var frostbite = scn.GetShadowCard("frostbite");
		var frostbite2 = scn.GetShadowCard("frostbite2");
		var caradhras = scn.GetShadowCard("caradhras");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, frostbite);
		scn.AttachCardsTo(frodo, frostbite2);
		scn.MoveCardsToHand(caradhras);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(4);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// 4 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		// Cruel Caradhras is a weather card and not a condition.
		scn.ShadowPlayCard(caradhras);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsChoose("burden");

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, caradhras);
		// 8 - 2 to play + 2 for each of the two copies of Frostbite
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DiscardedByAFreePeoplesEventWoundsItsBearerAndNoOneElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var frostbite = scn.GetShadowCard("frostbite");
		var frostbite2 = scn.GetShadowCard("frostbite2");
		var merry = scn.GetFreepsCard("merry");
		var seven = scn.GetFreepsCard("seven");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(frodo, frostbite);
		scn.AttachCardsTo(merry, frostbite2);
		scn.MoveCardsToHand(seven);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(seven);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frostbite, frostbite2));
		scn.FreepsChooseCard(frostbite);

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, frostbite);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The other copy and its bearer are as they were.
		assertAttachedTo(frostbite2, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedByASpecialAbilityOfAFreePeoplesCardWoundsItsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frostbite = scn.GetShadowCard("frostbite");
		var frostbite2 = scn.GetShadowCard("frostbite2");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var burdens = scn.GetFreepsCard("burdens");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveCardsToSupportArea(burdens);
		scn.AttachCardsTo(gimli, frostbite);
		scn.AttachCardsTo(aragorn, frostbite2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		int deckBefore = scn.GetFreepsDeckCount();

		// The Dwarf Guard pays, so that the only wound Gimli can take is Frostbite's.  Gimli's is the one
		// Shadow condition on a Dwarf, and is discarded without a choice.
		scn.FreepsUseCardAction(burdens);
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(deckBefore - 3, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, frostbite);
		assertEquals(1, scn.GetWoundsOn(gimli));
		// The other copy and its bearer are as they were.
		assertAttachedTo(frostbite2, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardedByAShadowCardDoesNotWoundItsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var frostbite = scn.GetShadowCard("frostbite");
		var frostbite2 = scn.GetShadowCard("frostbite2");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, frostbite);
		scn.AttachCardsTo(frodo, frostbite2);
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 set + 2 companions + 2 for the shadow number of site 2: enough for Saruman's 4 + 2 roaming
		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// "When you play Saruman, you may discard a condition."
		scn.ShadowPlayCard(saruman);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(frostbite, frostbite2));
		scn.ShadowChooseCard(frostbite);

		assertInZone(Zone.DISCARD, frostbite);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(frostbite2, frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardedByTheFreePeoplesPlayerWithASitesTextDoesNotWoundItsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHeightsOfIsengardScenario();

		var frodo = scn.GetRingBearer();
		var frostbite = scn.GetShadowCard("frostbite");
		var frostbite2 = scn.GetShadowCard("frostbite2");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, frostbite);
		scn.AttachCardsTo(frodo, frostbite2);

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The Shadow player lays the next site, and the fellowship moves to it.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals("Heights of Isengard", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, savage);
		// 7 against 5: a [gandalf] companion wins, and the site lets the Free Peoples player discard a condition.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frostbite, frostbite2));
		scn.FreepsChooseCard(frostbite);

		// A site is not a Free Peoples card.
		assertInZone(Zone.DISCARD, frostbite);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertAttachedTo(frostbite2, frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
