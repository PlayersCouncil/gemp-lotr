package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_138_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snows", "51_138");
					put("snows2", "51_138");    // a second copy, cheated onto another site, for the end-of-turn discard

					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc; a regroup ability only ever used on purpose
					put("commander", "1_186");  // Guard Commander: [moria] Orc, strength 7; Skirmish: remove (3) to make a [moria] Orc strength +2
					put("drums", "1_168");      // Drums in the Deep: Shadow skirmish event, cost 0: make a [moria] Orc strength +2

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: Skirmish: exert Gimli to make him strength +2
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text once played; cheated in, to skirmish the Orc
					put("swordsman", "1_117");  // Swordsman of the Northern Kingdom: Free Peoples skirmish event, cost 0: make a ranger strength +2
					put("songs", "1_61");       // Songs of the Blessed Realm: Free Peoples condition in the support area, to see that only Snows is discarded at the end of the turn
				}},
				// Site 2 is Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2",
				// a skirmish special ability each player is offered in every skirmish there.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansSnowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Snows
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Spell.</b> <b>Weather.</b> To play, exert an [isengard] minion. Plays on a site.
		 * No player may play skirmish events or use skirmish special abilities during skirmishes involving an [isengard] minion at this site.
		 * Discard this condition at the end of the turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("snows");

		assertEquals("Saruman's Snows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.WEATHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionToPlayOnTheSiteAndBarsSkirmishActionsOnlyInSkirmishesOfAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snows = scn.GetShadowCard("snows");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		var commander = scn.GetShadowCard("commander");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCardsToHand(snows, drums);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var swordsman = scn.GetFreepsCard("swordsman");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);
		scn.MoveCardsToHand(swordsman);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant, commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(16, scn.GetTwilight());    // 10 set + 4 companions + 2 for Ettenmoors
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertTrue(scn.ShadowPlayAvailable(snows));

		scn.ShadowPlayCard(snows);
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders, aragorn));
		scn.ShadowChooseCard(site2);

		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(commander));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(raiders);

		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertAttachedTo(snows, site2);
		assertEquals(14, scn.GetTwilight());    // 16 - 2 to play
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{gimli, commander}, new PhysicalCardImpl[]{guard, servant});
		scn.ShadowDeclineAssignments();

		// The skirmish of the [isengard] minion: neither player may play a skirmish event or use a skirmish special ability.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, raiders));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordsman));
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(site2));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(drums));
		assertFalse(scn.ShadowActionAvailable(commander));
		assertFalse(scn.ShadowActionAvailable(site2));
		scn.ShadowPass();

		// Strength 9 against 8: Aragorn loses and takes 1 + 1 for damage.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The skirmish of an [isengard] minion of another race, an Orc: barred in the same way.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, servant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordsman));
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(site2));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(drums));
		assertFalse(scn.ShadowActionAvailable(commander));
		assertFalse(scn.ShadowActionAvailable(site2));
		scn.ShadowPass();

		// The skirmish of the [moria] minion at the same site: each of them is offered.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, commander));
		assertTrue(scn.FreepsPlayAvailable(swordsman));
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsActionAvailable(site2));
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(drums));
		assertTrue(scn.ShadowActionAvailable(commander));
		assertTrue(scn.ShadowActionAvailable(site2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OnASiteTheFellowshipIsNotAtItBarsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snows = scn.GetShadowCard("snows");
		var raiders = scn.GetShadowCard("raiders");
		var commander = scn.GetShadowCard("commander");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCardsToHand(snows, drums);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var swordsman = scn.GetFreepsCard("swordsman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(swordsman);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowPlayCard(snows);
		scn.ShadowChooseCard(site1);
		assertAttachedTo(snows, site1);
		assertEquals(1, scn.GetWoundsOn(raiders));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{gimli, commander});

		// The fellowship is at site 2; the skirmish of the [isengard] minion is not at the site bearing Snows.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, raiders));
		assertTrue(scn.FreepsPlayAvailable(swordsman));
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsActionAvailable(site2));
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(drums));
		assertTrue(scn.ShadowActionAvailable(commander));
		assertTrue(scn.ShadowActionAvailable(site2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snows = scn.GetShadowCard("snows");
		var raiders = scn.GetShadowCard("raiders");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(snows);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, commander);
		scn.AddWoundsToChar(raiders, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Raiding Party has vitality 3: with 2 wounds it cannot exert.  The [moria] minion beside it could.
		assertEquals(13, scn.GetTwilight());    // 10 set + 1 companion + 2 for Ettenmoors
		assertFalse(scn.ShadowPlayAvailable(snows));
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysOnItsSiteThroughTheRegroupPhaseAndAMoveAndIsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snows = scn.GetShadowCard("snows");
		var snows2 = scn.GetShadowCard("snows2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(snows);

		var songs = scn.GetFreepsCard("songs");
		scn.MoveCardsToSupportArea(songs);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowPlayCard(snows);
		scn.ShadowChooseCard(site2);
		scn.AttachCardsTo(site1, snows2);
		assertAttachedTo(snows, site2);
		assertAttachedTo(snows2, site1);
		assertInZone(Zone.SUPPORT, songs);

		// With no minion left there is no combat, and the turn goes on to the regroup phase.
		scn.MoveCardsToDiscard(raiders);
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(snows, site2);

		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(snows, site2);

		scn.SkipToOtherPlayersTurn();
		// Each copy is discarded by its own trigger, and nothing else in play is.
		assertInDiscard(snows, snows2);
		assertInZone(Zone.SUPPORT, songs);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
