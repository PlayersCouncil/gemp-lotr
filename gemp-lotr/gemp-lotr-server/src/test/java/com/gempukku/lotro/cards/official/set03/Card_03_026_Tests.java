package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("draws", "3_26");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, vitality 3; her response is only for the Ring-bearer
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, vitality 2; her text needs Freeps' site 3 in play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion; a maneuver ability used only on purpose
					put("shards", "3_44");      // The Shards of Narsil: [gondor] artifact in the support area; fellowship abilities used only on purpose

					// The Palantir of Orthanc: [isengard] artifact, support area; a Shadow ability used only on purpose
					put("palantir", "3_67");
					// Scaffolding: [isengard] possession, support area; answers only an [isengard] Orc discarded or wounded
					put("scaffolding", "6_73");
					// Uruk-hai Armory: [isengard] condition, support area; the fellowship archery total -1 while an Uruk-hai is spotted
					put("armory", "1_157");
					// Spider Poison: [gollum] possession, support area; a skirmish ability for a companion skirmishing Shelob
					put("poison", "8_28");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1
					// Uruk-hai Sword: [isengard] possession, hand weapon for an Uruk-hai; acts only when its bearer wins
					put("sword", "1_160");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SomethingDrawsNearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Something Draws Near
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship or Maneuver
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Maneuver:</b> Exert an Elf to discard an [isengard] possession or [isengard] artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("draws");

		assertEquals("Something Draws Near", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheFellowshipPhaseAnElfExertsToDiscardAnIsengardArtifactInASupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetFreepsCard("draws");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shards = scn.GetFreepsCard("shards");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(draws);

		var palantir = scn.GetShadowCard("palantir");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var armory = scn.GetShadowCard("armory");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(palantir, scaffolding, armory, poison);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, palantir, scaffolding, armory, poison, shards);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertTrue(scn.FreepsPlayAvailable(draws));
		scn.FreepsPlayCard(draws);

		// An Elf companion or an Elf ally pays; the Man and the Hobbit do not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(silinde);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// An [isengard] artifact and an [isengard] possession; not an [isengard] condition, a possession of another
		// culture or an artifact of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(palantir, scaffolding));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, poison, shards));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(palantir);

		assertInZone(Zone.DISCARD, palantir, draws);
		assertInZone(Zone.SUPPORT, scaffolding, armory, poison, shards);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseItDiscardsAnIsengardPossessionBorneByAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetFreepsCard("draws");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(draws);

		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var scaffolding = scn.GetShadowCard("scaffolding");
		scn.MoveCardsToSupportArea(scaffolding);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, sword);
		assertAttachedTo(sword, savage);
		assertInZone(Zone.SUPPORT, scaffolding);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(draws));
		scn.FreepsPlayCard(draws);
		// Arwen, the only Elf, exerts unasked.
		assertEquals(1, scn.GetWoundsOn(arwen));

		// The borne possession and the one in the support area; not the [isengard] minion, which is no item.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, scaffolding));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));
		scn.FreepsChooseCard(sword);

		assertInZone(Zone.DISCARD, sword, draws);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, scaffolding);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetFreepsCard("draws");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(draws);

		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		scn.FreepsRefreshDecision();

		// Vitality 3 with 2 wounds: Arwen cannot exert.  Aragorn could, but is no Elf; the Palantir is there to discard.
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, palantir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(draws));
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetFreepsCard("draws");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(draws);

		var savage = scn.GetShadowCard("savage");
		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, palantir);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(draws));

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SUPPORT, palantir);
		assertFalse(scn.FreepsPlayAvailable(draws));
	}

	@Test
	public void CannotBePlayedInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetFreepsCard("draws");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(draws);

		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, palantir);
		assertTrue(scn.FreepsPlayAvailable(draws));

		// No minion: the regroup phase of the same turn comes straight on, the phase before the next Fellowship phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SUPPORT, palantir);
		assertFalse(scn.FreepsPlayAvailable(draws));
	}

	@Test
	public void WithNoIsengardPossessionOrArtifactInPlayTheElfExertsAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetFreepsCard("draws");
		var arwen = scn.GetFreepsCard("arwen");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(draws);

		var armory = scn.GetShadowCard("armory");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(armory, poison);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, armory, poison, shards);
		assertEquals(0, scn.GetWoundsOn(arwen));
		// A target is no requirement: the event is played for nothing (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(draws));
		scn.FreepsPlayCard(draws);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.DISCARD, draws);
		assertInZone(Zone.SUPPORT, armory, poison, shards);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
