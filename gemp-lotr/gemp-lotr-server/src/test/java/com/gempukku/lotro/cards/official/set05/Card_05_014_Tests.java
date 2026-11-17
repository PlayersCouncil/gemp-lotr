package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "5_14");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion; +2 only at sites 6 to 8
					put("lorienelf", "1_53");   // Lorien Elf: Elf companion with no text but "To play, spot an Elf"
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability used only on purpose
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally; a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, left unassigned so that it stands in the regroup phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThatIsNoOrcHornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: That Is No Orc Horn
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Spot 3 Elf companions to heal a companion and liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("That Is No Orc Horn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedWithThreeElfCompanionsItHealsACompanionAndLiberatesTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var gimli = scn.GetFreepsCard("gimli");
		var celeborn = scn.GetFreepsCard("celeborn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// The Shadow player controls sites 1 and 2, and the Free Peoples player the highest, site 3.
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		// Laid for the Shadow phase and assigned to nobody, it is still in play in the regroup phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(troop, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);

		// Companions of two races and cultures are offered; a wounded Elf ally and a wounded minion are no companions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, troop));
		scn.FreepsChooseCard(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(troop));
		// CR 4.2 "liberate": the highest-numbered site an opponent controls, with nothing asked.
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertInZone(Zone.DISCARD, horn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWithTwoElfCompanionsAndAnElfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var gimli = scn.GetFreepsCard("gimli");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(arwen, haldir, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		scn.SkipToSite(4);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddWoundsToChar(arwen, 1);
		scn.FreepsRefreshDecision();

		// A companion to heal and a site to liberate, but three Elves of whom only two are companions.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.HAND, horn);
		assertFalse(scn.FreepsPlayAvailable(horn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.ShadowControls(site2));
	}

	@Test
	public void WithNoCompanionWoundedItIsStillPlayedAndLiberatesASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		scn.SkipToSite(4);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		scn.SkipToPhase(Phase.REGROUP);
		// Only the ally is wounded.
		scn.AddWoundsToChar(celeborn, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.DISCARD, horn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsItHealsAndLiberatesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		// The only controlled site is the Free Peoples player's own.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddWoundsToChar(arwen, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);
		// The only wounded companion is healed unasked.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertInZone(Zone.DISCARD, horn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
