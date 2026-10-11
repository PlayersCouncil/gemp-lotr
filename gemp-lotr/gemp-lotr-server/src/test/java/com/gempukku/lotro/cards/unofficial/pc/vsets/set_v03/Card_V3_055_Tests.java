package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_055_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("schooner1", "103_55");
					put("schooner2", "103_55");
					put("gunners", "8_55");       // Corsair Gunners: Corsair, strength 10; a skirmish ability never used here
					put("explorer", "4_250");     // Southron Explorer: a [raider] Man who is no Corsair; a regroup ability never used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text at all
					put("galley", "8_59");        // Corsair War Galley: [raider] support-area possession that holds [raider] tokens; "Corsair" is in its title only
					put("ships", "8_65");         // Ships of Great Draught: [raider] support-area possession, cost 2; Shadow: remove a threat to add a [raider] token to it
					put("fleet", "18_66");        // Fleet of Corsair Ships: a [men] possession, so the tokens it holds are [men] tokens
					put("throne", "17_105");      // Throne of the Dark Lord: a Shadow artifact; its text needs Sauron or Mount Doom

					put("aragorn", "1_89");       // Aragorn, Ranger of the North
					put("athelas", "1_94");       // Athelas: [gondor] possession borne by a [gondor] Man; a fellowship ability
					put("toby", "1_305");         // Old Toby: [shire] possession in the support area; its text is only for when it is played
					put("toby2", "1_305");
					put("anduril", "7_79");       // Anduril, Flame of the West: [gondor] artifact borne by Aragorn
					put("shards", "3_44");        // The Shards of Narsil: [gondor] artifact in the support area; fellowship abilities
					put("coat", "2_105");         // Mithril-coat: [shire] artifact borne by Frodo
					put("lord", "1_21");          // Lord of Moria: a Free Peoples condition in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RaidingSchoonerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Raiding Schooner
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: When you play this, spot a Corsair to add a [raider] token here.
		 * 	Shadow: Remove 2 [raider] tokens from your cards to hinder a Free Peoples possession.
		 * 	Maneuver: Spot a Corsair, hinder another Raiding Schooner, and remove 3 [raider] tokens from your cards
		 * 	to hinder a Free Peoples artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("schooner1");

		assertEquals("Raiding Schooner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		// Two dots: no more than two copies in play
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForTwoToTheSupportAreaAndSpottingACorsairAddsARaiderTokenToItselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var ships = scn.GetShadowCard("ships");
		var gunners = scn.GetShadowCard("gunners");
		scn.MoveCardsToSupportArea(schooner1);
		scn.AddTokensToCard(schooner1, 2);
		scn.MoveCardsToHand(schooner2, ships);

		scn.StartGame();
		scn.MoveMinionsToTable(gunners);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.HAND, schooner2);
		assertEquals(2, scn.GetCultureTokensOn(schooner1));
		assertTrue(scn.ShadowPlayAvailable(schooner2));

		scn.ShadowPlayCard(schooner2);

		// 13 - 2 for the Schooner
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, schooner2);
		// The token goes on the copy that was played, and nobody was asked.
		assertEquals(1, scn.GetCultureTokensOn(schooner2));
		assertEquals(2, scn.GetCultureTokensOn(schooner1));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another card played with both Schooners and the Corsair in place adds nothing to either.
		scn.ShadowPlayCard(ships);

		// 11 - 2 for the Ships
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, ships);
		assertEquals(1, scn.GetCultureTokensOn(schooner2));
		assertEquals(2, scn.GetCultureTokensOn(schooner1));
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GetsNoTokenWithNoCorsairToSpotAndOneOnceTheHinderedCorsairIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var galley = scn.GetShadowCard("galley");
		var explorer = scn.GetShadowCard("explorer");
		var gunners = scn.GetShadowCard("gunners");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(galley);
		scn.MoveCardsToHand(schooner1, schooner2);

		scn.StartGame();
		// A [raider] Man who is no Corsair, a possession with "Corsair" in its title, a Corsair who is
		// hindered and so cannot be spotted, and an Orc.
		scn.MoveMinionsToTable(explorer, gunners, troop);
		scn.HinderCard(gunners);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(schooner1));

		scn.ShadowPlayCard(schooner1);

		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, schooner1);
		assertEquals(0, scn.GetCultureTokensOn(schooner1));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Restored, the Corsair can be spotted.
		scn.RestoreCard(gunners);

		scn.ShadowPlayCard(schooner2);

		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, schooner2);
		assertEquals(1, scn.GetCultureTokensOn(schooner2));
		assertEquals(0, scn.GetCultureTokensOn(schooner1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesTwoRaiderTokensFromAnyOfYourCardsToHinderAFreePeoplesPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var galley = scn.GetShadowCard("galley");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		var gunners = scn.GetShadowCard("gunners");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var anduril = scn.GetFreepsCard("anduril");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var shards = scn.GetFreepsCard("shards");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas, anduril);
		scn.MoveCardsToSupportArea(toby, toby2, shards, lord);
		scn.HinderCard(toby2);
		scn.MoveCardsToSupportArea(schooner1, schooner2, galley, ships, fleet);
		scn.AddTokensToCard(schooner1, 2);
		scn.AddTokensToCard(galley, 2);
		// Tokens that are out of reach: on a hindered card, and of another culture.
		scn.AddTokensToCard(ships, 2);
		scn.HinderCard(ships);
		scn.AddTokensToCard(fleet, 2);

		scn.StartGame();
		// With a Corsair, a second Schooner, more than 3 [raider] tokens and artifacts to hinder, everything
		// the Maneuver ability asks for is here too; this is the Shadow phase.
		scn.MoveMinionsToTable(gunners);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(toby));
		assertTrue(scn.ShadowActionAvailable(schooner1));

		scn.ShadowUseCardAction(schooner1);

		// The first token
		assertTrue(scn.ShadowHasCardChoiceAvailable(schooner1, galley));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(schooner2, ships, fleet));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(galley);

		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(schooner1));

		// The second, from another card
		assertTrue(scn.ShadowHasCardChoiceAvailable(schooner1, galley));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(schooner2, ships, fleet));
		scn.ShadowChooseCard(schooner1);

		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(schooner1));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(fleet));
		// Two tokens and no more, and no Schooner hindered: this was the Shadow ability.
		assertFalse(scn.IsHindered(schooner2));

		// A possession borne by a companion and one in the support area, of two cultures. Not the one already
		// hindered, the artifacts, the condition, or the Shadow player's own possessions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(athelas, toby));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby2, anduril, shards, lord, schooner1, schooner2, galley));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(toby);

		assertTrue(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(lord));
		assertFalse(scn.IsHindered(schooner1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilTwoRaiderTokensAreWithinReach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToSupportArea(schooner1, schooner2, ships, fleet);
		scn.AddTokensToCard(schooner1, 1);
		// Out of reach: 2 [raider] tokens on a hindered Schooner, and 2 [men] tokens.
		scn.AddTokensToCard(schooner2, 2);
		scn.HinderCard(schooner2);
		scn.AddTokensToCard(fleet, 2);
		scn.AddThreats(1);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(1, scn.GetCultureTokensOn(schooner1));
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertFalse(scn.ShadowActionAvailable(schooner1));

		// Ships of Great Draught: "Remove a threat ... to add a [raider] token here."
		scn.ShadowUseCardAction(ships);

		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.ShadowActionAvailable(schooner1));

		// One token on each of two cards: the first is asked for, and the second can only be the other.
		scn.ShadowUseCardAction(schooner1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(schooner1, ships));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(schooner2, fleet));
		scn.ShadowChooseCard(ships);

		// Old Toby is the only possession, so it is hindered without a choice.
		assertEquals(0, scn.GetCultureTokensOn(schooner1));
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(schooner2));
		assertEquals(2, scn.GetCultureTokensOn(fleet));
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsOfferedWithNoPossessionToHinderAndStillTakesItsTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);
		scn.MoveCardsToSupportArea(schooner1);
		scn.AddTokensToCard(schooner1, 2);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCultureTokensOn(schooner1));
		assertTrue(scn.ShadowActionAvailable(schooner1));

		scn.ShadowUseCardAction(schooner1);

		assertEquals(0, scn.GetCultureTokensOn(schooner1));
		// The only Free Peoples item in play is an artifact.
		assertFalse(scn.IsHindered(anduril));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityHindersTheOtherSchoonerAndRemovesThreeRaiderTokensToHinderAFreePeoplesArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var galley = scn.GetShadowCard("galley");
		var throne = scn.GetShadowCard("throne");
		var gunners = scn.GetShadowCard("gunners");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var anduril = scn.GetFreepsCard("anduril");
		var coat = scn.GetFreepsCard("coat");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas, anduril);
		scn.HinderCard(anduril);
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToSupportArea(schooner1, schooner2, galley, throne);
		scn.AddTokensToCard(schooner1, 4);
		scn.AddTokensToCard(galley, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(gunners);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.IsHindered(schooner2));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(shards));
		assertTrue(scn.ShadowActionAvailable(schooner1));

		scn.ShadowUseCardAction(schooner1);

		// There is one other Schooner, so it is hindered without a choice; the Galley is no Schooner.
		assertTrue(scn.IsHindered(schooner2));
		assertFalse(scn.IsHindered(schooner1));
		assertFalse(scn.IsHindered(galley));

		// Three tokens, from either card that has them
		assertTrue(scn.ShadowHasCardChoiceAvailable(schooner1, galley));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(schooner2));
		scn.ShadowChooseCard(schooner1);
		scn.ShadowChooseCard(galley);
		scn.ShadowChooseCard(schooner1);

		assertEquals(2, scn.GetCultureTokensOn(schooner1));
		assertEquals(1, scn.GetCultureTokensOn(galley));

		// An artifact borne by the Ring-bearer and one in the support area, of two cultures. Not the one
		// already hindered, a possession, a Shadow artifact, or The One Ring.
		assertTrue(scn.ShadowHasCardChoiceAvailable(coat, shards));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(anduril, athelas, throne, ring));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(coat);

		assertTrue(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(throne));
		assertFalse(scn.IsHindered(ring));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();

		// A Corsair and 3 tokens are still here, but the only other Schooner is hindered now.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(3, scn.GetCultureTokensOn(schooner1) + scn.GetCultureTokensOn(galley));
		assertFalse(scn.ShadowActionAvailable(schooner1));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithNoCorsairToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var galley = scn.GetShadowCard("galley");
		var explorer = scn.GetShadowCard("explorer");
		var gunners = scn.GetShadowCard("gunners");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToSupportArea(schooner1, schooner2, galley);
		scn.AddTokensToCard(schooner1, 3);

		scn.StartGame();
		// A [raider] Man who is no Corsair, a possession with "Corsair" in its title, and a hindered Corsair
		scn.MoveMinionsToTable(explorer, gunners);
		scn.HinderCard(gunners);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.IsHindered(schooner2));
		assertEquals(3, scn.GetCultureTokensOn(schooner1));
		assertFalse(scn.ShadowActionAvailable(schooner1));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedToALoneSchooner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var galley = scn.GetShadowCard("galley");
		var gunners = scn.GetShadowCard("gunners");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);
		// Another [raider] possession in the support area that is no Schooner
		scn.MoveCardsToSupportArea(schooner1, galley);
		scn.AddTokensToCard(schooner1, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(gunners);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// "Another" Schooner: it cannot pay by hindering itself.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(3, scn.GetCultureTokensOn(schooner1));
		assertFalse(scn.ShadowActionAvailable(schooner1));
		assertFalse(scn.IsHindered(schooner1));
	}

	@Test
	public void NeitherAbilityIsOfferedInTheManeuverPhaseWithTwoRaiderTokensWithinReach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var galley = scn.GetShadowCard("galley");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		var gunners = scn.GetShadowCard("gunners");
		var toby = scn.GetFreepsCard("toby");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(toby, shards);
		scn.MoveCardsToSupportArea(schooner1, schooner2, galley, ships, fleet);
		scn.AddTokensToCard(schooner1, 1);
		scn.AddTokensToCard(galley, 1);
		// Out of reach: 2 [raider] tokens on a hindered card, and 2 [men] tokens.
		scn.AddTokensToCard(ships, 2);
		scn.HinderCard(ships);
		scn.AddTokensToCard(fleet, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(gunners);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Two tokens would pay for the Shadow ability, in the Shadow phase. The Maneuver ability needs three.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.IsHindered(schooner2));
		assertFalse(scn.ShadowActionAvailable(schooner1));
		assertFalse(scn.ShadowActionAvailable(schooner2));
	}

	@Test
	public void ManeuverAbilityIsPaidForWithThreeTokensOnAnotherOfYourCardsAndWithOneOtherSchoonerNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var galley = scn.GetShadowCard("galley");
		var gunners = scn.GetShadowCard("gunners");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);
		// Two dots: two Schooners are as many as a game can have in play (CR 5.0 "non-unique"), so the Schooner
		// used never has two others to choose between. Neither Schooner holds a token: all three are on the
		// Corsair War Galley.
		scn.MoveCardsToSupportArea(schooner1, schooner2, galley);
		scn.AddTokensToCard(galley, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(gunners);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(schooner1));
		assertEquals(0, scn.GetCultureTokensOn(schooner2));
		assertEquals(3, scn.GetCultureTokensOn(galley));
		assertFalse(scn.IsHindered(schooner2));
		assertFalse(scn.IsHindered(shards));
		assertTrue(scn.ShadowActionAvailable(schooner1));

		scn.ShadowUseCardAction(schooner1);

		// One other Schooner, the three tokens all on one card and one artifact: each can only be the one it
		// is, so nothing is asked. The Schooner hindered is the other and never the one used; the Galley, a
		// [raider] possession beside them, is no Schooner.
		assertTrue(scn.IsHindered(schooner2));
		assertFalse(scn.IsHindered(schooner1));
		assertFalse(scn.IsHindered(galley));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertTrue(scn.IsHindered(shards));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenTheTokensItNeedsAreOnTheSchoonerItMustHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var schooner1 = scn.GetShadowCard("schooner1");
		var schooner2 = scn.GetShadowCard("schooner2");
		var gunners = scn.GetShadowCard("gunners");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToSupportArea(schooner1, schooner2);
		scn.AddTokensToCard(schooner1, 1);
		scn.AddTokensToCard(schooner2, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(gunners);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Three [raider] tokens in all. Whichever Schooner is used hinders the other first, and tokens on a
		// hindered card cannot be removed: 1 token is then within reach of the first, 2 of the second.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(schooner1));
		assertEquals(2, scn.GetCultureTokensOn(schooner2));
		assertFalse(scn.ShadowActionAvailable(schooner1));
		assertFalse(scn.ShadowActionAvailable(schooner2));
		assertFalse(scn.IsHindered(shards));
	}
}
