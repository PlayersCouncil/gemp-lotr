package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("old", "2_69");
					put("old2", "2_69");
					put("old3", "2_69");

					// Legolas, Companion of the Ring: an Elf companion, strength 6, no archer; his bonus is only at a
					// battleground or forest, which no site here is.
					put("legolas", "11_21");
					// Gimli, Son of Gloin: a Dwarf companion, strength 6; a skirmish ability used only on purpose.
					put("gimli", "1_13");
					// Aragorn, Ranger of the North: a Man companion, strength 8; a maneuver ability used only on purpose.
					put("aragorn", "1_89");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow
					// him to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
					// Elrond, Lord of Rivendell: an Elf ally, to spot and, at his home site 3, to skirmish; his
					// start-of-turn heal and his fellowship ability reach no reading here.
					put("elrond", "1_40");

					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; "may add (2)" only when played.
					put("runner", "1_178");
					// Goblin Patrol Troop: a [moria] Orc of another title, strength 13, no text.
					put("troop", "1_177");
					// Orc Soldier: a [sauron] Orc, strength 7; a skirmish ability used only on purpose.
					put("soldier", "1_271");
					// Vile Tentacle: a [moria] Creature, no Orc; strength 7 -4 away from a marsh, which no site here is.
					put("vile", "101_36");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An Elf assigned to a [moria] Orc and a Dwarf assigned to an Uruk-hai, with two copies of the event in hand.
	protected VirtualTableScenario GetElfAndDwarfScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("differences1", "2_69");
					put("differences2", "2_69");
					put("arwen", "1_30");
					put("gimli", "1_13");
					put("runner", "1_178");
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OldDifferencesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Old Differences
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Elf or Dwarf skirmishing a [moria] Orc strength -1 (or -3 if you spot an Elf and a Dwarf).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("old");

		assertEquals("Old Differences", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "Make an Elf or Dwarf skirmishing a [moria] Orc strength -1": the definition took any Elf or Dwarf in play.
	@Test
	public void OnlyAnElfOrDwarfSkirmishingAMoriaOrcIsMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetElfAndDwarfScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);

		var differences1 = scn.GetShadowCard("differences1");
		var differences2 = scn.GetShadowCard("differences2");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(differences1, differences2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{gimli, savage});

		// Gimli is skirmishing an Uruk-hai, and Arwen's skirmish against the [moria] Orc has not begun: the event
		// finds nobody.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowPlayCard(differences1);
		assertInZone(Zone.DISCARD, differences1);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 6 against 5: the Savage takes Gimli's two wounds and lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		// Arwen is skirmishing the [moria] Orc and is the only one who is: she is chosen with nothing asked, and
		// with an Elf and a Dwarf to spot she is strength -3.
		scn.FreepsResolveSkirmish(arwen);
		scn.FreepsPass();
		scn.ShadowPlayCard(differences2);
		assertEquals(3, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnElfSkirmishingAMoriaOrcWithNoDwarfToSpotIsStrengthMinusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		var old = scn.GetShadowCard("old");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(old);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		// A skirmish event: not playable at the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(old));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, runner}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(legolas);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(old));
		scn.ShadowPlayCard(old);

		// Legolas is the one Elf or Dwarf skirmishing a [moria] Orc, so nothing is asked.  An Elf is in play and
		// no Dwarf (Aragorn is a Man): -1, not -3.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, old);

		scn.FreepsPass();
		scn.ShadowPass();

		// With the skirmish over he is himself again.
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ADwarfSkirmishingAMoriaOrcWithNoElfToSpotIsStrengthMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var old = scn.GetShadowCard("old");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(old);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(old));
		scn.ShadowPlayCard(old);

		// A Dwarf is in play and no Elf: -1.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, old);
	}

	@Test
	public void ADwarfAllySkirmishingAMoriaOrcWithAnElfAllyToSpotIsStrengthMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(thrarin, elrond);

		var old = scn.GetShadowCard("old");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(old);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Thrarin's own ability lets him skirmish.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, runner);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(elrond));
		assertTrue(scn.ShadowPlayAvailable(old));
		scn.ShadowPlayCard(old);

		// Thrarin is the Dwarf and Elrond, in the support area, the Elf spotted: -3.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(elrond));
		assertInZone(Zone.DISCARD, old);
	}

	@Test
	public void AnElfAllyAtHisHomeSiteSkirmishingAMoriaOrcIsStrengthMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		var old = scn.GetShadowCard("old");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(old);

		scn.StartGame();
		// Elrond's home is site 3, where the fellowship comes to stand: he may skirmish there.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, runner);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(elrond));
		assertTrue(scn.ShadowPlayAvailable(old));
		scn.ShadowPlayCard(old);

		// An Elf in play and no Dwarf to spot: -1, not -3.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(elrond));
		assertInZone(Zone.DISCARD, old);
	}

	@Test
	public void AgainstAManAnOrcOfAnotherCultureOrAMoriaMinionWhoIsNoOrcItChangesNothing()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli);

		var old = scn.GetShadowCard("old");
		var old2 = scn.GetShadowCard("old2");
		var old3 = scn.GetShadowCard("old3");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		var vile = scn.GetShadowCard("vile");
		scn.MoveCardsToHand(old, old2, old3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, soldier, vile);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{legolas, soldier},
				new PhysicalCardImpl[]{gimli, vile});

		// An Elf and a Dwarf are in play throughout, so anyone wrongly reached would be strength -3.

		// Aragorn, a Man, against a [moria] Orc: the event is played, and nobody is weakened.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(old));
		scn.ShadowPlayCard(old);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, old);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPass();
		scn.ShadowPass();

		// Legolas, an Elf, against a [sauron] Orc.
		scn.FreepsResolveSkirmish(legolas);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.ShadowPlayAvailable(old2));
		scn.ShadowPlayCard(old2);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, old2);
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPass();
		scn.ShadowPass();

		// Gimli, a Dwarf, against a [moria] minion who is no Orc.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowPlayAvailable(old3));
		scn.ShadowPlayCard(old3);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, old3);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(legolas));
	}
}
