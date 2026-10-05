package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("saruman", "101_30");

					put("snows", "1_138");      // Saruman's Snows: weather condition that plays on a site; stops skirmish events and skirmish abilities at that site
					put("frost", "1_135");      // Saruman's Frost: weather condition that plays on a site; Hobbits at that site are strength -2
					put("ambition", "1_133");   // Saruman's Ambition: an [isengard] condition that is not weather; cheapens [isengard] events
					put("spies", "1_140");      // Spies of Saruman: an [isengard] condition that plays on a site and is not weather; the archery phase is skipped at its site, and it is discarded at the end of the turn
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("flock", "101_25");     // Crebain Flock: [isengard] Crow, strength 2, vitality 3; a Shadow ability never used here
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text at all
					put("armory", "1_173");     // Goblin Armory: [moria] condition; triggers on [moria] weapons and [moria] Orcs only

					put("greenleaf", "1_50");   // Legolas, Greenleaf: archer; "Archery: Exert Legolas to wound a minion; Legolas does not add to the fellowship archery total."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, vitality 3; a skirmish ability never used here
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: ally, vitality 2; a maneuver ability never used here
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; his text only changes his own strength
					put("betrayal", "3_29");    // Betrayal of Isengard: "Maneuver: Exert Gandalf to wound Saruman twice."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Saruman, Fell Voice
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 6
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: While you cannot spot a weather, Saruman may not take wounds in the archery phase and may not be assigned to a skirmish.
		 * 	While at a site bearing a weather, each [isengard] minion is strength +2.
		 * 	Response: If the fellowship moves from a site bearing a weather, discard this minion to exert each companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Fell Voice", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsFourAndTwoMoreForRoamingToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, saruman);

		scn.ShadowPlayCard(saruman);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		// 10 - 4 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWeatherSarumanTakesNoWoundInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var snows = scn.GetShadowCard("snows");
		var ambition = scn.GetShadowCard("ambition");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(greenleaf, aragorn);
		// A weather that is in hand and an [isengard] condition in play that is not weather: neither is a weather to spot.
		scn.MoveCardsToHand(snows);
		scn.MoveCardsToSupportArea(ambition);
		// A second archer, so that there is archery fire after Legolas has used his ability
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.ARCHER));

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInZone(Zone.HAND, snows);
		assertInZone(Zone.SUPPORT, ambition);
		assertEquals(0, scn.GetWoundsOn(saruman));

		// A wound from a card in the archery phase
		scn.FreepsUseCardAction(greenleaf);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		// Archery fire: Aragorn's one arrow, Legolas no longer counting
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.PassArcheryActions();
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithAWeatherInPlaySarumanCanBeWoundedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var frost = scn.GetShadowCard("frost");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(greenleaf, aragorn);
		// A second archer, so that there is archery fire after Legolas has used his ability
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.ARCHER));

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, troop);
		scn.FreepsPassCurrentPhaseAction();
		// The weather is on the site the fellowship has left: it need only be somewhere to be spotted.
		scn.AttachCardsTo(scn.GetSite(1), frost);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertAttachedTo(frost, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsUseCardAction(greenleaf);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, savage, troop));
		scn.FreepsChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));

		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.PassArcheryActions();
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, savage, troop));
		scn.ShadowChooseCard(saruman);

		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoWeatherSarumanCanStillBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var betrayal = scn.GetFreepsCard("betrayal");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsActionAvailable(betrayal));

		scn.FreepsUseCardAction(betrayal);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoWeatherSarumanCannotBeAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var snows = scn.GetShadowCard("snows");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable("aragorn");
		// A weather that is in hand and an [isengard] condition in play that is not weather: neither is a weather to spot.
		scn.MoveCardsToHand(snows);
		scn.MoveCardsToSupportArea(ambition);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.SkipToAssignments();

		// The Free Peoples player is offered the Uruk Savage alone.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsCanAssign(savage));
		assertFalse(scn.FreepsCanAssign(saruman));

		// Nor can the Shadow player assign Saruman as a minion left over.
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowCanAssign(savage));
		assertFalse(scn.ShadowCanAssign(saruman));

		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAWeatherInPlaySarumanCanBeAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var snows = scn.GetShadowCard("snows");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(snows);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.FreepsCanAssign(saruman));

		// Saruman's Snows: "To play, exert an [isengard] minion. Plays on a site."
		scn.ShadowPlayCard(snows);
		scn.ShadowChooseCard(scn.GetSite(2));
		scn.ShadowChooseCard(savage);
		scn.SkipToAssignments();

		assertAttachedTo(snows, scn.GetSite(2));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsCanAssign(savage));
		assertTrue(scn.FreepsCanAssign(saruman));

		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(2, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowCanAssign(saruman));

		scn.ShadowAssignToMinions(aragorn, saruman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, saruman));
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayingAWeatherOnTheFellowshipsSiteMakesEachIsengardMinionStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var flock = scn.GetShadowCard("flock");
		var troop = scn.GetShadowCard("troop");
		var snows = scn.GetShadowCard("snows");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(snows);
		scn.MoveCompanionsToTable(aragorn);
		// No [isengard] card but a minion has a strength, so Aragorn is made [isengard] as well as [gondor].
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ISENGARD));

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, flock, troop);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(saruman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(2, scn.GetStrength(flock));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		// Frodo's 3 and 1 for The One Ring
		assertEquals(4, scn.GetStrength(frodo));

		// Saruman's Snows: "To play, exert an [isengard] minion. Plays on a site."
		scn.ShadowPlayCard(snows);
		scn.ShadowChooseCard(scn.GetSite(2));
		scn.ShadowChooseCard(savage);

		assertAttachedTo(snows, scn.GetSite(2));
		// Saruman himself, an Uruk-hai and a Crow are [isengard] minions. The [moria] Orc is a minion of another
		// culture, the [isengard] Aragorn is not a minion, and Frodo is neither.
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(7, scn.GetStrength(savage));
		assertEquals(4, scn.GetStrength(flock));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWeatherPlayedOnAnotherSiteGivesNoStrengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var flock = scn.GetShadowCard("flock");
		var snows = scn.GetShadowCard("snows");
		scn.MoveCardsToHand(snows);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage, flock);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(saruman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(2, scn.GetStrength(flock));

		// The same play as on the fellowship's site, but on the site it has left
		scn.ShadowPlayCard(snows);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scn.GetSite(1), scn.GetSite(2)));
		scn.ShadowChooseCard(scn.GetSite(1));
		scn.ShadowChooseCard(savage);

		assertAttachedTo(snows, scn.GetSite(1));
		assertEquals(6, scn.GetStrength(saruman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(2, scn.GetStrength(flock));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseDiscardsSarumanToExertEachCompanionWhenTheFellowshipLeavesASiteBearingAWeather() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var snows = scn.GetShadowCard("snows");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.FreepsPassCurrentPhaseAction();
		scn.AttachCardsTo(scn.GetCurrentSite(), snows);
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertAttachedTo(snows, scn.GetSite(2));
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, saruman);
		// The Ring-bearer and companions of two other cultures each exert; the ally is not a companion.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoResponseWhenTheSiteLeftBearsAConditionThatIsNotAWeatherAndTheWeatherIsOnAnotherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var frost = scn.GetShadowCard("frost");
		var spies = scn.GetShadowCard("spies");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.FreepsPassCurrentPhaseAction();
		// The weather is on the site the fellowship has already left. The site it is at bears Spies of Saruman,
		// which plays on a site as a weather does and is not one. (Nobody here is an archer, so the archery
		// phase it skips is not missed.)
		scn.AttachCardsTo(scn.GetSite(1), frost);
		scn.AttachCardsTo(scn.GetSite(2), spies);
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertAttachedTo(frost, scn.GetSite(1));
		assertAttachedTo(spies, scn.GetSite(2));
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);

		scn.FreepsChooseToMove();

		// The fellowship has left site 2, which bears no weather, and the game has gone on with no response offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoResponseWhenTheWeatherIsOnTheSiteTheFellowshipMovesTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// This is played on the second player's turn, whose fellowship moves onto sites the first player has
		// already laid down. The first player is then the Shadow player, so Saruman and the weather are his.
		var saruman = scn.GetFreepsCard("saruman");
		var frost = scn.GetFreepsCard("frost");
		var frodo = scn.GetShadowRingBearer();
		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// The first player's fellowship moves twice, to site 3, and his turn ends.
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship moves from site 1 to site 2.
		scn.ShadowPassCurrentPhaseAction();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(saruman);
		// A weather may be played on any site in play; this one is on the next site along.
		scn.AttachCardsTo(scn.GetSite(3), frost);
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertAttachedTo(frost, scn.GetSite(3));
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);

		scn.ShadowChooseToMove();

		// The fellowship has moved from site 2, which bears nothing, to the site bearing the weather, and the
		// game has gone on with no response offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(frost, scn.GetSite(3));
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The first player is the one asked for Shadow actions on this turn.
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void ACardOfAnotherCultureMadeWeatherServesAsWellAsAnIsengardOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var armory = scn.GetShadowCard("armory");
		var frodo = scn.GetRingBearer();
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(greenleaf, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(6, scn.GetStrength(saruman));
		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.FreepsCanAssign(saruman));

		// Every weather card there is belongs to [isengard]. The text asks only for "a weather", so a [moria]
		// condition is put on the fellowship's site and made one. (The keyword is given once the card is in
		// play: one given to a card still in the draw deck is lost when the card is moved.)
		scn.AttachCardsTo(scn.GetSite(2), armory);
		assertEquals(6, scn.GetStrength(saruman));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, armory, null, Keyword.WEATHER));

		assertTrue(scn.HasKeyword(armory, Keyword.WEATHER));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(7, scn.GetStrength(savage));

		// Legolas is the fellowship's one archer, and the Shadow player may give his arrow to Saruman.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, savage));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));

		scn.PassAssignmentActions();
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsCanAssign(saruman));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, saruman);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(greenleaf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
