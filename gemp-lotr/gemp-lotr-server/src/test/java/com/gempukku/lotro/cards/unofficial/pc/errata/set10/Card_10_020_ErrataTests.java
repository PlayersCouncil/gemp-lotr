package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_020_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strike1", "60_20");
					put("strike2", "60_20");
					put("gollum", "6_40");          // Gollum, Old Villain: [gollum], cost 2, vitality 4, home site 3; a skirmish ability used only on purpose
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: the one Gollum who is not [gollum] ([wraith]); cost 2
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: a [gollum] minion who is not Gollum; cost 6, vitality 8, home site 8

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("gimli", "1_13");           // Gimli, Son of Gloin: strength 6; "Skirmish: Exert Gimli to make him strength +2."
					put("bowmen1", "3_41");         // Gondor Bowmen: support-area condition; "Archery: Exert a [gondor] companion and discard this condition to make the fellowship archery total +2."
					put("bowmen2", "3_41");
					put("saga", "1_114");           // The Saga of Elendil: [gondor] tale on a [gondor] companion; a skirmish ability used only on purpose
					put("valor", "1_103");          // Elendil's Valor: Maneuver event, cost 0; "Make a [gondor] companion defender +1 until the regroup phase."
					put("assassin", "2_93");        // Tower Assassin: [sauron] Orc, strength 11, vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
					put("wreathed", "1_238");       // Wreathed in Shadow: [wraith] support-area condition; "Archery: Remove (1) to make the fellowship archery total -1." (to play, spot a Nazgul: cheated in)
				}},
				// Site 4 is Balin's Tomb ("Maneuver: Discard your tale from play or from hand to heal your companion"),
				// site 8 is Anduin Banks (a river) and site 9 is Emyn Muil, which has no terrain of its own.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strike1", "60_20");
				}},
				// Site 8 is Watchers of Cirith Ungol (no terrain) and site 9 is Dagorlad: a plains, which adds 3 threats
				// when the fellowship moves there.
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The formats themselves, for what each makes of this card and of the Final Strike it replaces.
	protected DeckValidationScenario GetPCMovieScenario() {
		return new DeckValidationScenario(DeckValidationScenario.PCMovie);
	}

	protected DeckValidationScenario GetPCKingBlockScenario() {
		return new DeckValidationScenario("pc_king_block");
	}

	protected DeckValidationScenario GetPCExpandedScenario() {
		return new DeckValidationScenario("pc_expanded");
	}

	protected DeckValidationScenario GetMovieScenario() {
		return new DeckValidationScenario("movie");
	}

	@Test
	public void FinalStrikeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Final Strike
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Any site 9 is a <b>mountain</b>.
		 * <b>Shadow:</b> If the fellowship is at any site 9, play Gollum from your discard pile (limit once per phase).
		 * <b>Response:</b> If the Free Peoples player uses a maneuver or archery special ability, exert Gollum to
		 * cancel its effect.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strike1");

		assertEquals("Final Strike", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertTrue(card.getBlueprint().getGameText().contains("(limit once per phase)"));
	}

	@Test
	public void PlayedForOneToTheSupportAreaAndMakesSite9AMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var strike = scn.GetShadowCard("strike1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToHand(strike);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		var site8 = scn.GetSite(8);
		var site9 = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(site9, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site9, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site8, Keyword.MOUNTAIN));
		// 5 set + 1 companion + 9 for the site
		assertEquals(15, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(strike));

		scn.ShadowPlayCard(strike);
		assertEquals(14, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.SUPPORT, strike);

		// Dagorlad is a mountain as well as a plains; the site before it is neither.
		assertTrue(scn.HasKeyword(site9, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(site9, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site8, Keyword.MOUNTAIN));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityPlaysAGollumOfEitherCultureFromTheDiscardPileAtSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike1");
		var gollum = scn.GetShadowCard("gollum");
		var wraithGollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(strike);
		scn.MoveCardsToDiscard(gollum, wraithGollum, shelob);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertEquals(9, scn.GetCurrentSiteNumber());
		// Emyn Muil is a mountain too, and Anduin Banks behind it is not.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(scn.GetSite(8), Keyword.MOUNTAIN));
		// 1 companion + 9 for the site: enough for Shelob (6), who is at home at site 8 and does not roam here.
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(strike));

		scn.ShadowUseCardAction(strike);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, wraithGollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCardBPFromSelection(wraithGollum);
		assertInZone(Zone.SHADOW_CHARACTERS, wraithGollum);
		assertInZone(Zone.DISCARD, gollum, shelob);
		// Gollum costs 2, and his home is site 3: no roaming at site 9.
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, strike);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedAtSite8AndIsOfferedOneMoveLater() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike1");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(strike);
		scn.MoveCardsToDiscard(gollum);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 6 for the site: Gollum could be paid for.
		assertEquals(17, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(strike));

		// No minions, so the turn goes straight to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, gollum);
		assertTrue(scn.ShadowActionAvailable(strike));
	}

	@Test
	public void ShadowAbilityNeedsAGollumInTheDiscardPileAndHisCostInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike1");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(strike);
		scn.MoveCardsToDiscard(shelob);
		scn.MoveCardsToHand(gollum);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertEquals(9, scn.GetCurrentSiteNumber());
		// 1 companion + 9 for the site. Gollum is in hand and Shelob, who could be paid for, is in the discard pile.
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(strike));

		// Gollum in the discard pile, with 1 in the pool against his cost of 2.
		scn.MoveCardsToDiscard(gollum);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(strike));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(strike));

		// The one Gollum there is, is played without a choice.
		scn.ShadowUseCardAction(strike);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInZone(Zone.DISCARD, shelob);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyMayBeUsedOnceInAShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike1 = scn.GetShadowCard("strike1");
		var strike2 = scn.GetShadowCard("strike2");
		var gollum = scn.GetShadowCard("gollum");
		var wraithGollum = scn.GetShadowCard("wraithgollum");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(strike1, strike2);
		scn.MoveCardsToDiscard(gollum, wraithGollum);
		scn.SetTwilight(0);
		scn.FreepsPass();

		// 1 companion + 9 for the site
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(strike1));
		assertTrue(scn.ShadowActionAvailable(strike2));

		scn.ShadowUseCardAction(strike1);
		scn.ShadowChooseCardBPFromSelection(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(8, scn.GetTwilight());

		// Gollum is unique: while he is in play the other Gollum in the discard pile cannot be played, so not even
		// the unused copy is offered.
		assertInZone(Zone.DISCARD, wraithGollum);
		assertFalse(scn.ShadowActionAvailable(strike1));
		assertFalse(scn.ShadowActionAvailable(strike2));

		// Gollum is back in the discard pile in the same Shadow phase: the copy already used stays shut, and the
		// other is on offer.
		scn.MoveCardsToDiscard(gollum);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(strike1));
		assertTrue(scn.ShadowActionAvailable(strike2));

		scn.ShadowUseCardAction(strike2);
		scn.ShadowChooseCardBPFromSelection(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(6, scn.GetTwilight());

		scn.MoveCardsToDiscard(gollum);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(strike1));
		assertFalse(scn.ShadowActionAvailable(strike2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseExertsGollumToCancelAManeuverAbilityWhoseCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var strike = scn.GetShadowCard("strike1");
		var gollum = scn.GetShadowCard("gollum");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(strike);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(gollum));

		// Left alone, Aragorn's ability does what it says.
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(strike));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(gollum));

		// Cancelled, it has still been paid for, and gives him nothing more.
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(strike));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ResponseCancelsAManeuverAbilityOfASiteTheFreePeoplesPlayerUses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var strike = scn.GetShadowCard("strike1");
		var gollum = scn.GetShadowCard("gollum");
		// Gondor Bowmen is a tale as well: kept out of the hand, so that the site's cost has only the tale in play
		// to take and is paid without a question.
		var bowmen1 = scn.GetFreepsCard("bowmen1");
		var bowmen2 = scn.GetFreepsCard("bowmen2");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga);
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveCardsToDiscard(bowmen1, bowmen2);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(strike);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.MANEUVER);

		// Balin's Tomb is a site: its ability is the Free Peoples player's, by no card of his side.
		var tomb = scn.GetCurrentSite();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsActionAvailable(tomb));

		scn.FreepsUseCardAction(tomb);
		assertInZone(Zone.DISCARD, saga);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(strike));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gollum));
		// The tale is gone and Aragorn is not healed.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ResponseCancelsAnArcheryAbilityOfACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bowmen1 = scn.GetFreepsCard("bowmen1");
		var bowmen2 = scn.GetFreepsCard("bowmen2");
		var strike = scn.GetShadowCard("strike1");
		var wreathed = scn.GetShadowCard("wreathed");
		// Gollum, Half a Wraith Himself is the one Gollum who is not [gollum]: it is he who pays here, as Gollum,
		// Old Villain does in the maneuver test.
		var wraithGollum = scn.GetShadowCard("wraithgollum");

		// Aragorn is the one [gondor] companion, so it is he who exerts for Gondor Bowmen.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(bowmen1, bowmen2);

		scn.StartGame();
		scn.MoveCardsToSupportArea(strike, wreathed);
		scn.MoveMinionsToTable(wraithGollum);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(bowmen1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(strike));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(wraithGollum));

		scn.ShadowPass();
		scn.FreepsUseCardAction(bowmen2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(strike));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(wraithGollum));
		// The second Gondor Bowmen was paid for in full and added nothing.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, bowmen1, bowmen2);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// An archery special ability, but the Shadow player's own: Gollum could pay, and the response is not offered.
		scn.SetTwilight(1);
		scn.ShadowUseCardAction(wreathed);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(strike));
		assertEquals(1, scn.GetWoundsOn(wraithGollum));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedForAShadowAbilityAManeuverEventOrASkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor = scn.GetFreepsCard("valor");
		var strike = scn.GetShadowCard("strike1");
		var gollum = scn.GetShadowCard("gollum");
		var assassin = scn.GetShadowCard("assassin");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(valor);

		scn.StartGame();
		scn.MoveCardsToSupportArea(strike);
		scn.MoveMinionsToTable(gollum, assassin);
		scn.SkipToPhase(Phase.MANEUVER);

		// On this board the response is there to be had.
		scn.FreepsUseCardAction(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(strike));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		// A maneuver special ability, but the Shadow player's own.
		assertEquals(0, scn.GetWoundsOn(assassin));
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(strike));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The Free Peoples player's maneuver event, which is no special ability. Aragorn is the one [gondor] companion.
		scn.FreepsPlayCard(valor);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(strike));
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The Free Peoples player's skirmish special ability.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gimli, gollum }, new PhysicalCardImpl[] { aragorn, assassin });
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(strike));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileGollumIsExhaustedThoughShelobCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var strike = scn.GetShadowCard("strike1");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(strike);
		scn.MoveMinionsToTable(gollum, shelob);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gollum, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(aragorn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(strike));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// One wound fewer, and Gollum can pay: it is he who exerts, not Shelob.
		scn.RemoveWoundsFromChar(gollum, 1);
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(strike));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PcFormatsUseTheErrataAndOldDeckListsConvert() {
		// PC formats with errata set 60 map the original to the errata; a deck list saved with 10_20 is converted
		// when it is validated or joins a game, and the errata itself is legal there
		for (var dvs : new DeckValidationScenario[] { GetPCMovieScenario(), GetPCKingBlockScenario(), GetPCExpandedScenario() }) {
			var format = dvs.getFormat();
			String code = format.getCode();
			assertEquals(code, "60_20", format.getErrataCardMap().get("10_20"));
			assertEquals(code, "60_20", format.applyErrata("10_20"));
			assertNull(code, format.validateCard("60_20"));
		}

		// Decipher formats keep the original card and do not accept the PC errata
		var movie = GetMovieScenario().getFormat();
		assertEquals("10_20", movie.applyErrata("10_20"));
		assertNull(movie.validateCard("10_20"));
		assertNotNull(movie.validateCard("60_20"));
	}
}
